package com.nebula.search;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Iterative PageRank with dangling-node redistribution and convergence checks. */
public final class PageRank {
    private PageRank() { }

    public static PageRankResult compute(LinkGraph graph) {
        return compute(graph, 0.85, 100, 1e-9);
    }

    public static PageRankResult compute(LinkGraph graph, double damping, int maxIterations, double tolerance) {
        if (graph == null) throw new IllegalArgumentException("graph must not be null");
        if (damping < 0.0 || damping > 1.0) throw new IllegalArgumentException("damping must be between zero and one");
        if (maxIterations <= 0 || tolerance <= 0.0) throw new IllegalArgumentException("invalid convergence settings");
        Map<String, Double> current = new LinkedHashMap<>();
        for (String node : graph.nodes()) current.put(node, 1.0 / Math.max(1, graph.nodes().size()));
        if (current.isEmpty()) return new PageRankResult(current, 0, true);

        boolean converged = false;
        int iterations = 0;
        for (iterations = 1; iterations <= maxIterations; iterations++) {
            Map<String, Double> next = new LinkedHashMap<>();
            double base = (1.0 - damping) / current.size();
            for (String node : current.keySet()) next.put(node, base);
            double danglingMass = 0.0;
            for (String source : current.keySet()) {
                if (graph.outgoing(source).isEmpty()) danglingMass += current.get(source);
            }
            double redistributed = damping * danglingMass / current.size();
            for (String node : next.keySet()) next.put(node, next.get(node) + redistributed);
            for (String source : current.keySet()) {
                Set<String> outgoing = graph.outgoing(source);
                if (outgoing.isEmpty()) continue;
                double share = damping * current.get(source) / outgoing.size();
                for (String target : outgoing) {
                    if (next.containsKey(target)) next.put(target, next.get(target) + share);
                }
            }
            double difference = 0.0;
            for (String node : current.keySet()) difference += Math.abs(next.get(node) - current.get(node));
            current = next;
            if (difference <= tolerance) {
                converged = true;
                break;
            }
        }
        return new PageRankResult(current, iterations, converged);
    }
}
