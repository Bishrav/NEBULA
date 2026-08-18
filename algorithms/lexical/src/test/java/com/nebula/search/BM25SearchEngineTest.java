package com.nebula.search;

import com.nebula.ingestion.DocumentIngestor;
import com.nebula.ingestion.DocumentRecord;

import java.util.List;

/** Dependency-free tests for BM25 relevance, top-k behavior, and explanations. */
public final class BM25SearchEngineTest {
    public static void main(String[] args) {
        DocumentIngestor ingestor = new DocumentIngestor();
        DocumentRecord relevant = ingestor.ingest("docs/relevant.md",
                "# Distributed Search\n\nDistributed search uses shards and a query coordinator.");
        DocumentRecord partial = ingestor.ingest("docs/partial.md",
                "# Search Notes\n\nSearch results should be explainable.");
        DocumentRecord unrelated = ingestor.ingest("docs/unrelated.md",
                "# Deployment\n\nDeploy services with health checks.");

        InvertedIndex index = new InvertedIndex();
        index.add(relevant);
        index.add(partial);
        index.add(unrelated);

        List<SearchResult> results = new BM25SearchEngine(index).search("distributed search", 2);
        check(results.size() == 2, "top-k limit is applied");
        check(results.get(0).getDocument().getDocumentId().equals(relevant.getDocumentId()), "best document ranks first");
        check(results.get(0).getScore() > results.get(1).getScore(), "scores are ordered");
        check(results.get(0).getTermContributions().containsKey("distributed"), "term contribution is explained");
        check(new BM25SearchEngine(index).search("missing-term", 10).isEmpty(), "unknown terms return no results");
        System.out.println("BM25SearchEngineTest: PASS");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
