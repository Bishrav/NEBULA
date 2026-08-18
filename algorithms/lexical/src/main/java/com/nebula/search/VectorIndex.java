package com.nebula.search;

import com.nebula.ingestion.DocumentRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Exact brute-force vector index used as the ANN correctness baseline. */
public final class VectorIndex {
    private final int dimension;
    private final Map<String, VectorDocument> documentsById = new LinkedHashMap<>();

    public VectorIndex(int dimension) {
        if (dimension <= 0) throw new IllegalArgumentException("dimension must be positive");
        this.dimension = dimension;
    }

    public synchronized boolean add(DocumentRecord document, double[] vector, String modelId) {
        validateDimension(vector);
        if (documentsById.containsKey(document.getDocumentId())) return false;
        documentsById.put(document.getDocumentId(), new VectorDocument(document, vector, modelId));
        return true;
    }

    public synchronized List<VectorMatch> search(double[] queryVector, int limit) {
        validateDimension(queryVector);
        if (limit <= 0) throw new IllegalArgumentException("limit must be positive");
        List<VectorMatch> matches = new ArrayList<>();
        for (VectorDocument document : documentsById.values()) {
            matches.add(new VectorMatch(document, cosineSimilarity(queryVector, document.getVector())));
        }
        Collections.sort(matches, new Comparator<VectorMatch>() {
            @Override
            public int compare(VectorMatch left, VectorMatch right) {
                int scoreOrder = Double.compare(right.getScore(), left.getScore());
                return scoreOrder != 0 ? scoreOrder
                        : left.getDocument().getDocument().getDocumentId().compareTo(right.getDocument().getDocumentId());
            }
        });
        return Collections.unmodifiableList(new ArrayList<>(matches.subList(0, Math.min(limit, matches.size()))));
    }

    public int dimension() { return dimension; }
    public synchronized int documentCount() { return documentsById.size(); }

    static double cosineSimilarity(double[] left, double[] right) {
        if (left.length != right.length) throw new IllegalArgumentException("vector dimensions differ");
        double dot = 0.0;
        double leftMagnitude = 0.0;
        double rightMagnitude = 0.0;
        for (int i = 0; i < left.length; i++) {
            dot += left[i] * right[i];
            leftMagnitude += left[i] * left[i];
            rightMagnitude += right[i] * right[i];
        }
        if (leftMagnitude == 0.0 || rightMagnitude == 0.0) return 0.0;
        return dot / (Math.sqrt(leftMagnitude) * Math.sqrt(rightMagnitude));
    }

    private void validateDimension(double[] vector) {
        if (vector == null || vector.length != dimension) throw new IllegalArgumentException("vector dimension mismatch");
    }

    public static final class VectorMatch {
        private final VectorDocument document;
        private final double score;

        private VectorMatch(VectorDocument document, double score) {
            this.document = document;
            this.score = score;
        }

        public VectorDocument getDocument() { return document; }
        public double getScore() { return score; }
    }
}
