package com.nebula.search;

import com.nebula.ingestion.DocumentRecord;

import java.util.Arrays;

/** Immutable document vector with model metadata. */
public final class VectorDocument {
    private final DocumentRecord document;
    private final double[] vector;
    private final String modelId;

    public VectorDocument(DocumentRecord document, double[] vector, String modelId) {
        if (document == null || vector == null || modelId == null) throw new IllegalArgumentException("vector document fields are required");
        this.document = document;
        this.vector = Arrays.copyOf(vector, vector.length);
        this.modelId = modelId;
    }

    public DocumentRecord getDocument() { return document; }
    public double[] getVector() { return Arrays.copyOf(vector, vector.length); }
    public String getModelId() { return modelId; }
}
