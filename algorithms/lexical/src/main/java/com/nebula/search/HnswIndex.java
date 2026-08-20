package com.nebula.search;

import com.nebula.ingestion.DocumentRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

/** Simplified deterministic multi-layer HNSW index for ANN experiments. */
public final class HnswIndex {
    private final int dimension;
    private final int m;
    private final int efConstruction;
    private final Random random;
    private final Map<String, Node> nodes = new LinkedHashMap<>();
    private Node entryPoint;
    private int maxLevel = -1;

    public HnswIndex(int dimension, int m, int efConstruction, long seed) {
        if (dimension <= 0 || m <= 0 || efConstruction <= 0) throw new IllegalArgumentException("invalid HNSW parameters");
        this.dimension = dimension;
        this.m = m;
        this.efConstruction = Math.max(efConstruction, m);
        this.random = new Random(seed);
    }

    public synchronized boolean add(DocumentRecord document, double[] vector, String modelId) {
        if (vector == null || vector.length != dimension) throw new IllegalArgumentException("vector dimension mismatch");
        if (nodes.containsKey(document.getDocumentId())) return false;
        Node node = new Node(new VectorDocument(document, vector, modelId), randomLevel());
        nodes.put(document.getDocumentId(), node);
        if (entryPoint == null) {
            entryPoint = node;
            maxLevel = node.level;
            return true;
        }

        Node current = entryPoint;
        for (int level = maxLevel; level > node.level; level--) {
            List<ScoredNode> nearest = searchLayer(vector, current, 1, level);
            if (!nearest.isEmpty()) current = nearest.get(0).node;
        }
        for (int level = Math.min(node.level, maxLevel); level >= 0; level--) {
            List<ScoredNode> neighbors = searchLayer(vector, current, efConstruction, level);
            connect(node, neighbors, level);
            if (!neighbors.isEmpty()) current = neighbors.get(0).node;
        }
        if (node.level > maxLevel) {
            entryPoint = node;
            maxLevel = node.level;
        }
        return true;
    }

    public synchronized List<HnswMatch> search(double[] queryVector, int limit, int efSearch) {
        if (queryVector == null || queryVector.length != dimension) throw new IllegalArgumentException("vector dimension mismatch");
        if (limit <= 0 || efSearch <= 0) throw new IllegalArgumentException("limit and efSearch must be positive");
        if (entryPoint == null) return Collections.emptyList();
        Node current = entryPoint;
        for (int level = maxLevel; level > 0; level--) {
            List<ScoredNode> nearest = searchLayer(queryVector, current, 1, level);
            if (!nearest.isEmpty()) current = nearest.get(0).node;
        }
        List<ScoredNode> nearest = searchLayer(queryVector, current, Math.max(efSearch, limit), 0);
        List<HnswMatch> matches = new ArrayList<>();
        for (ScoredNode scored : nearest) matches.add(new HnswMatch(scored.node.document, scored.score));
        return Collections.unmodifiableList(new ArrayList<>(matches.subList(0, Math.min(limit, matches.size()))));
    }

    public int dimension() { return dimension; }
    public int maxLevel() { return maxLevel; }
    public synchronized int documentCount() { return nodes.size(); }

    private void connect(Node node, List<ScoredNode> neighbors, int level) {
        Set<String> selected = node.neighbors(level);
        for (ScoredNode neighbor : neighbors) {
            if (neighbor.node == node) continue;
            selected.add(neighbor.node.document.getDocument().getDocumentId());
            neighbor.node.neighbors(level).add(node.document.getDocument().getDocumentId());
            prune(neighbor.node, level);
        }
        prune(node, level);
    }

    private void prune(Node node, int level) {
        Set<String> neighbors = node.neighbors(level);
        if (neighbors.size() <= m) return;
        List<String> ordered = new ArrayList<>(neighbors);
        Collections.sort(ordered, new Comparator<String>() {
            @Override
            public int compare(String left, String right) {
                double leftScore = VectorIndex.cosineSimilarity(node.document.getVector(), nodes.get(left).document.getVector());
                double rightScore = VectorIndex.cosineSimilarity(node.document.getVector(), nodes.get(right).document.getVector());
                return Double.compare(rightScore, leftScore);
            }
        });
        neighbors.clear();
        neighbors.addAll(ordered.subList(0, m));
    }

    private List<ScoredNode> searchLayer(double[] query, Node entry, int ef, int level) {
        Set<String> visited = new HashSet<>();
        PriorityQueue<ScoredNode> candidates = new PriorityQueue<>(Comparator.comparingDouble((ScoredNode value) -> value.score).reversed());
        PriorityQueue<ScoredNode> results = new PriorityQueue<>(Comparator.comparingDouble(value -> value.score));
        ScoredNode start = new ScoredNode(entry, VectorIndex.cosineSimilarity(query, entry.document.getVector()));
        candidates.add(start);
        results.add(start);
        visited.add(entry.document.getDocument().getDocumentId());
        while (!candidates.isEmpty()) {
            ScoredNode current = candidates.poll();
            if (results.size() >= ef && current.score < results.peek().score) break;
            for (String neighborId : current.node.neighbors(level)) {
                if (!visited.add(neighborId)) continue;
                Node neighbor = nodes.get(neighborId);
                if (neighbor == null) continue;
                ScoredNode scored = new ScoredNode(neighbor, VectorIndex.cosineSimilarity(query, neighbor.document.getVector()));
                if (results.size() < ef || scored.score > results.peek().score) {
                    candidates.add(scored);
                    results.add(scored);
                    if (results.size() > ef) results.poll();
                }
            }
        }
        List<ScoredNode> ordered = new ArrayList<>(results);
        Collections.sort(ordered, Comparator.comparingDouble((ScoredNode value) -> value.score).reversed());
        return ordered;
    }

    private int randomLevel() {
        int level = 0;
        while (level < 32 && random.nextDouble() < 0.5) level++;
        return level;
    }

    private static final class Node {
        private final VectorDocument document;
        private final int level;
        private final List<Set<String>> neighbors;

        private Node(VectorDocument document, int level) {
            this.document = document;
            this.level = level;
            this.neighbors = new ArrayList<>();
            for (int i = 0; i <= level; i++) neighbors.add(new LinkedHashSet<String>());
        }

        private Set<String> neighbors(int level) {
            return level <= this.level ? neighbors.get(level) : Collections.<String>emptySet();
        }
    }

    private static final class ScoredNode {
        private final Node node;
        private final double score;

        private ScoredNode(Node node, double score) {
            this.node = node;
            this.score = score;
        }
    }

    public static final class HnswMatch {
        private final VectorDocument document;
        private final double score;

        private HnswMatch(VectorDocument document, double score) {
            this.document = document;
            this.score = score;
        }

        public VectorDocument getDocument() { return document; }
        public double getScore() { return score; }
    }
}
