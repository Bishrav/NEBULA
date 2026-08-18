package com.nebula.search;

/** One user judgement about a retrieved document during a pilot session. */
public final class FeedbackRecord {
    private final String query;
    private final String mode;
    private final String documentId;
    private final String sourcePath;
    private final boolean useful;

    public FeedbackRecord(String query, String mode, String documentId, String sourcePath, boolean useful) {
        this.query = require(query, "query");
        this.mode = require(mode, "mode");
        this.documentId = require(documentId, "documentId");
        this.sourcePath = require(sourcePath, "sourcePath");
        this.useful = useful;
    }

    public String getQuery() { return query; }
    public String getMode() { return mode; }
    public String getDocumentId() { return documentId; }
    public String getSourcePath() { return sourcePath; }
    public boolean isUseful() { return useful; }

    private static String require(String value, String field) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException(field + " must not be blank");
        return value;
    }
}
