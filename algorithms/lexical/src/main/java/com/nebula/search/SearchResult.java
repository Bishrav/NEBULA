package com.nebula.search;

import com.nebula.ingestion.DocumentRecord;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Explainable ranked search result. */
public final class SearchResult {
    private final DocumentRecord document;
    private final double score;
    private final Map<String, Double> termContributions;

    public SearchResult(DocumentRecord document, double score, Map<String, Double> termContributions) {
        this.document = document;
        this.score = score;
        this.termContributions = Collections.unmodifiableMap(new LinkedHashMap<>(termContributions));
    }

    public DocumentRecord getDocument() { return document; }
    public double getScore() { return score; }
    public Map<String, Double> getTermContributions() { return termContributions; }
}
