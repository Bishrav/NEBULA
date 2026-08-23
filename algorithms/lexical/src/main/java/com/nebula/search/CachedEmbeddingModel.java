package com.nebula.search;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

/** Offline embedding adapter backed by a reproducible text-hash/vector cache. */
public final class CachedEmbeddingModel implements EmbeddingModel {
    private final EmbeddingModelMetadata metadata;
    private final Map<String, double[]> vectors;

    public CachedEmbeddingModel(Path cache) throws IOException {
        if (cache == null || !Files.isRegularFile(cache)) throw new IllegalArgumentException("embedding cache is required");
        Map<String, String> properties = new HashMap<>();
        Map<String, double[]> loaded = new HashMap<>();
        for (String line : Files.readAllLines(cache, StandardCharsets.UTF_8)) {
            if (line.isBlank()) continue;
            if (line.startsWith("#")) {
                int split = line.indexOf('=');
                if (split > 1) properties.put(line.substring(1, split).trim(), line.substring(split + 1).trim());
                continue;
            }
            String[] parts = line.split("\\t", 2);
            if (parts.length != 2) throw new IOException("invalid embedding cache row");
            String[] values = parts[1].split(",");
            double[] vector = new double[values.length];
            for (int i = 0; i < values.length; i++) vector[i] = Double.parseDouble(values[i]);
            if (loaded.put(parts[0], vector) != null) throw new IOException("duplicate embedding cache key");
        }
        int dimension = Integer.parseInt(required(properties, "dimension"));
        metadata = new EmbeddingModelMetadata(required(properties, "modelId"), required(properties, "revision"), required(properties, "license"), dimension, Boolean.parseBoolean(required(properties, "normalized")));
        Set<String> keys = new HashSet<>(loaded.keySet());
        if (keys.size() != loaded.size()) throw new IOException("duplicate embedding cache key");
        for (double[] vector : loaded.values()) {
            if (vector.length != dimension) throw new IOException("embedding dimension mismatch");
            double norm = 0.0;
            for (double value : vector) {
                if (!Double.isFinite(value)) throw new IOException("embedding cache contains non-finite value");
                norm += value * value;
            }
            if (metadata.isNormalized() && Math.abs(Math.sqrt(norm) - 1.0) > 0.001) throw new IOException("normalized embedding cache contains non-unit vector");
        }
        vectors = Map.copyOf(loaded);
    }

    @Override public double[] embed(String text) {
        if (text == null) throw new IllegalArgumentException("text must not be null");
        double[] vector = vectors.get(hash(text));
        if (vector == null) throw new IllegalArgumentException("text is absent from offline embedding cache");
        return vector.clone();
    }

    public EmbeddingModelMetadata metadata() { return metadata; }
    @Override public int dimension() { return metadata.getDimension(); }
    @Override public String modelId() { return metadata.getModelId() + "@" + metadata.getRevision(); }
    public int cachedVectors() { return vectors.size(); }

    public static String hash(String text) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(64);
            for (byte value : digest) result.append(String.format("%02x", value));
            return result.toString();
        } catch (NoSuchAlgorithmException exc) { throw new IllegalStateException("SHA-256 unavailable", exc); }
    }

    private static String required(Map<String, String> properties, String key) throws IOException {
        String value = properties.get(key);
        if (value == null || value.isBlank()) throw new IOException("embedding cache missing " + key);
        return value;
    }
}
