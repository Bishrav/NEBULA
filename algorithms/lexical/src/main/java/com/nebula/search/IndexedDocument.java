package com.nebula.search;

import com.nebula.ingestion.DocumentRecord;

/** Search-time metadata retained beside the inverted index. */
public final class IndexedDocument {
    private final DocumentRecord document;
    private final int documentLength;

    public IndexedDocument(DocumentRecord document, int documentLength) {
        this.document = document;
        this.documentLength = documentLength;
    }

    public DocumentRecord getDocument() { return document; }
    public int getDocumentLength() { return documentLength; }
}
