package com.nebula.evaluation;

import com.nebula.ingestion.DocumentRecord;
import com.nebula.search.LinkGraph;
import com.nebula.search.PageRank;
import com.nebula.search.PageRankResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Builds a deterministic document graph and summarizes PageRank health. */
public final class GraphBenchmark {
    public Result evaluate(List<DocumentRecord> documents) {
        LinkGraph graph = new LinkGraph();
        for (DocumentRecord document : documents) graph.add(document);
        PageRankResult pageRank = PageRank.compute(graph);
        int dangling = 0;
        for (String node : graph.nodes()) if (graph.outgoing(node).isEmpty()) dangling++;
        double mass = 0.0;
        for (double score : pageRank.getScores().values()) mass += score;
        List<String> top = new ArrayList<>(pageRank.topNodes(5));
        Collections.sort(top);
        return new Result(graph.nodes().size(), graph.edgeCount(), dangling, pageRank.getIterations(),
                pageRank.isConverged(), mass, top);
    }

    public static final class Result {
        private final int nodes;
        private final int edges;
        private final int dangling;
        private final int iterations;
        private final boolean converged;
        private final double scoreMass;
        private final List<String> topNodes;

        private Result(int nodes, int edges, int dangling, int iterations, boolean converged,
                       double scoreMass, List<String> topNodes) {
            this.nodes = nodes; this.edges = edges; this.dangling = dangling;
            this.iterations = iterations; this.converged = converged; this.scoreMass = scoreMass;
            this.topNodes = Collections.unmodifiableList(new ArrayList<>(topNodes));
        }
        public int getNodes() { return nodes; }
        public int getEdges() { return edges; }
        public int getDangling() { return dangling; }
        public int getIterations() { return iterations; }
        public boolean isConverged() { return converged; }
        public double getScoreMass() { return scoreMass; }
        public List<String> getTopNodes() { return topNodes; }
    }
}
