package com.nebula.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** PageRank scores and convergence metadata for a link graph. */
public final class PageRankResult {
    private final Map<String, Double> scores;
    private final int iterations;
    private final boolean converged;

    PageRankResult(Map<String, Double> scores, int iterations, boolean converged) {
        this.scores = Collections.unmodifiableMap(new LinkedHashMap<>(scores));
        this.iterations = iterations;
        this.converged = converged;
    }

    public double score(String node) { return scores.containsKey(node) ? scores.get(node) : 0.0; }
    public Map<String, Double> getScores() { return scores; }
    public int getIterations() { return iterations; }
    public boolean isConverged() { return converged; }

    public List<String> topNodes(int limit) {
        List<String> nodes = new ArrayList<>(scores.keySet());
        Collections.sort(nodes, new Comparator<String>() {
            @Override
            public int compare(String left, String right) {
                int scoreOrder = Double.compare(scores.get(right), scores.get(left));
                return scoreOrder != 0 ? scoreOrder : left.compareTo(right);
            }
        });
        return Collections.unmodifiableList(new ArrayList<>(nodes.subList(0, Math.min(limit, nodes.size()))));
    }
}
