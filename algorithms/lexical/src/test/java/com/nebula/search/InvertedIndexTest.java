package com.nebula.search;

import com.nebula.ingestion.DocumentIngestor;
import com.nebula.ingestion.DocumentRecord;

/** Dependency-free tests for analysis, postings, positions, and corpus statistics. */
public final class InvertedIndexTest {
    public static void main(String[] args) {
        DocumentIngestor ingestor = new DocumentIngestor();
        DocumentRecord first = ingestor.ingest("docs/one.md", "# Search\n\nSearch systems need ranking.");
        DocumentRecord second = ingestor.ingest("docs/two.md", "# Ranking\n\nRanking improves search quality.");

        InvertedIndex index = new InvertedIndex();
        check(index.add(first), "first document is indexed");
        check(index.add(second), "second document is indexed");
        check(!index.add(first), "duplicate document is ignored");
        check(index.documentCount() == 2, "document count is tracked");
        check(index.documentFrequency("search") == 2, "document frequency is tracked");
        check(index.postings("search").get(0).getTermFrequency() == 2, "term frequency is tracked");
        check(index.postings("search").get(0).getPositions().equals(java.util.Arrays.asList(0, 1)), "positions are tracked");
        check(index.averageDocumentLength() > 0.0, "average document length is tracked");
        System.out.println("InvertedIndexTest: PASS");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
