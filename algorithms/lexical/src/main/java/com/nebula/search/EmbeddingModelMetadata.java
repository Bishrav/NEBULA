package com.nebula.search;

/** Immutable provenance metadata for an embedding model used in an experiment. */
public final class EmbeddingModelMetadata {
    private final String modelId;
    private final String revision;
    private final String license;
    private final int dimension;
    private final boolean normalized;

    public EmbeddingModelMetadata(String modelId, String revision, String license, int dimension, boolean normalized) {
        if (modelId == null || modelId.isBlank() || revision == null || revision.isBlank() || license == null || license.isBlank() || dimension <= 0) {
            throw new IllegalArgumentException("complete embedding metadata is required");
        }
        this.modelId = modelId; this.revision = revision; this.license = license; this.dimension = dimension; this.normalized = normalized;
    }

    public String getModelId() { return modelId; }
    public String getRevision() { return revision; }
    public String getLicense() { return license; }
    public int getDimension() { return dimension; }
    public boolean isNormalized() { return normalized; }

    public String toJson() {
        return String.format("{\"schemaVersion\":\"embedding-model-metadata-v1\",\"modelId\":\"%s\",\"revision\":\"%s\",\"license\":\"%s\",\"dimension\":%d,\"normalized\":%s}",
                escape(modelId), escape(revision), escape(license), dimension, normalized);
    }

    private static String escape(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\""); }
}
