package com.nebula.ingestion;

import java.util.Objects;

/** Immutable, normalized representation of an ingested document. */
public final class DocumentRecord {
    private final String documentId;
    private final String sourcePath;
    private final String sourceType;
    private final String title;
    private final String text;
    private final String contentHash;

    public DocumentRecord(String documentId, String sourcePath, String sourceType,
                          String title, String text, String contentHash) {
        this.documentId = requireValue(documentId, "documentId");
        this.sourcePath = requireValue(sourcePath, "sourcePath");
        this.sourceType = requireValue(sourceType, "sourceType");
        this.title = requireValue(title, "title");
        this.text = requireValue(text, "text");
        this.contentHash = requireValue(contentHash, "contentHash");
    }

    public String getDocumentId() { return documentId; }
    public String getSourcePath() { return sourcePath; }
    public String getSourceType() { return sourceType; }
    public String getTitle() { return title; }
    public String getText() { return text; }
    public String getContentHash() { return contentHash; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof DocumentRecord)) return false;
        DocumentRecord that = (DocumentRecord) other;
        return documentId.equals(that.documentId);
    }

    @Override
    public int hashCode() { return Objects.hash(documentId); }

    @Override
    public String toString() {
        return "DocumentRecord{" + documentId + ", title='" + title + "'}";
    }

    private static String requireValue(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}
