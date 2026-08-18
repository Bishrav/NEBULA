package com.nebula.search;

/** Embedding boundary so the deterministic baseline can later be replaced by a model. */
public interface EmbeddingModel {
    double[] embed(String text);
    int dimension();
    String modelId();
}
