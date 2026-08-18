package com.nebula.ingestion;

/** Document metadata plus its monotonic source version. */
public final class VersionedDocument {
    private final DocumentRecord document;
    private final int version;
    private final boolean changed;

    public VersionedDocument(DocumentRecord document, int version, boolean changed) {
        this.document = document;
        this.version = version;
        this.changed = changed;
    }

    public DocumentRecord getDocument() { return document; }
    public int getVersion() { return version; }
    public boolean isChanged() { return changed; }
}
