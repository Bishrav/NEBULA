package com.nebula.search;

import java.util.List;

/** Dependency-free feature-hashing embedding baseline for correctness experiments. */
public final class HashingEmbeddingModel implements EmbeddingModel {
    private final int dimension;

    public HashingEmbeddingModel(int dimension) {
        if (dimension <= 0) throw new IllegalArgumentException("dimension must be positive");
        this.dimension = dimension;
    }

    @Override
    public double[] embed(String text) {
        double[] vector = new double[dimension];
        List<String> terms = TextAnalyzer.analyze(text);
        for (String term : terms) {
            int bucket = Math.floorMod(term.hashCode(), dimension);
            int sign = (term.hashCode() & 1) == 0 ? 1 : -1;
            vector[bucket] += sign;
        }
        normalize(vector);
        return vector;
    }

    @Override
    public int dimension() { return dimension; }

    @Override
    public String modelId() { return "hashing-v1-d" + dimension; }

    static void normalize(double[] vector) {
        double magnitude = 0.0;
        for (double value : vector) magnitude += value * value;
        magnitude = Math.sqrt(magnitude);
        if (magnitude == 0.0) return;
        for (int i = 0; i < vector.length; i++) vector[i] /= magnitude;
    }
}
