package com.nebula.search;

import com.nebula.ingestion.DocumentIngestor;
import com.nebula.ingestion.DocumentRecord;

import java.util.List;

/** Application boundary joining document normalization with lexical search. */
public final class SearchCatalog {
    private final DocumentIngestor ingestor;
    private final InvertedIndex index;
    private final BM25SearchEngine searchEngine;

    public SearchCatalog() {
        this(new DocumentIngestor(), new InvertedIndex());
    }

    public SearchCatalog(DocumentIngestor ingestor, InvertedIndex index) {
        this.ingestor = ingestor;
        this.index = index;
        this.searchEngine = new BM25SearchEngine(index);
    }

    public DocumentRecord indexMarkdown(String sourcePath, String content) {
        DocumentRecord document = ingestor.ingest(sourcePath, content);
        index.add(document);
        return document;
    }

    public List<SearchResult> search(String query, int limit) {
        return searchEngine.search(query, limit);
    }

    public int documentCount() {
        return index.documentCount();
    }
}
