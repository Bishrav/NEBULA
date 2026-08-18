package com.nebula.search;

import java.util.Locale;

/** Dependency-free subword embedding baseline using hashed character n-grams. */
public final class CharacterNgramEmbeddingModel implements EmbeddingModel {
    private final int dimension;
    private final int minNgram;
    private final int maxNgram;

    public CharacterNgramEmbeddingModel(int dimension) {
        this(dimension, 3, 5);
    }

    public CharacterNgramEmbeddingModel(int dimension, int minNgram, int maxNgram) {
        if (dimension <= 0) throw new IllegalArgumentException("dimension must be positive");
        if (minNgram <= 0 || maxNgram < minNgram) {
            throw new IllegalArgumentException("invalid n-gram range");
        }
        this.dimension = dimension;
        this.minNgram = minNgram;
        this.maxNgram = maxNgram;
    }

    @Override
    public double[] embed(String text) {
        if (text == null) throw new IllegalArgumentException("text must not be null");
        double[] vector = new double[dimension];
        String normalized = normalize(text);
        for (int start = 0; start < normalized.length(); start++) {
            if (normalized.charAt(start) == ' ') continue;
            for (int length = minNgram; length <= maxNgram; length++) {
                int end = start + length;
                if (end > normalized.length()) break;
                String ngram = normalized.substring(start, end);
                if (ngram.indexOf(' ') >= 0) continue;
                int hash = ngram.hashCode();
                int bucket = Math.floorMod(hash, dimension);
                int sign = (hash & 1) == 0 ? 1 : -1;
                vector[bucket] += sign;
            }
        }
        HashingEmbeddingModel.normalize(vector);
        return vector;
    }

    @Override
    public int dimension() { return dimension; }

    @Override
    public String modelId() {
        return "char-ngram-v1-d" + dimension + "-" + minNgram + "-" + maxNgram;
    }

    private String normalize(String text) {
        StringBuilder normalized = new StringBuilder(text.length());
        boolean previousSpace = true;
        String lower = text.toLowerCase(Locale.ROOT);
        for (int i = 0; i < lower.length(); i++) {
            char character = lower.charAt(i);
            if (Character.isLetterOrDigit(character)) {
                normalized.append(character);
                previousSpace = false;
            } else if (!previousSpace) {
                normalized.append(' ');
                previousSpace = true;
            }
        }
        if (normalized.length() > 0 && normalized.charAt(normalized.length() - 1) == ' ') {
            normalized.setLength(normalized.length() - 1);
        }
        return normalized.toString();
    }
}
