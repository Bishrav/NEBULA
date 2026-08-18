package com.nebula.search;

import com.nebula.ingestion.DocumentIngestor;
import java.nio.file.Files;
import java.nio.file.Path;

/** Round-trip test for immutable segment writing and loading. */
public final class IndexSegmentPersistenceTest {
    public static void main(String[] args) throws Exception {
        InvertedIndex index = new InvertedIndex();
        DocumentIngestor ingestor = new DocumentIngestor();
        index.add(ingestor.ingest("docs/a.md", "# Search\n\nSearch uses positions and compression."));
        index.add(ingestor.ingest("docs/b.md", "# Ranking\n\nBM25 ranks search results."));

        Path directory = Files.createTempDirectory("nebula-segment-");
        Path segment = directory.resolve("segment-000001.idx");
        try {
            new IndexSegmentWriter().write(segment, index);
            PersistedIndexSegment loaded = new IndexSegmentReader().read(segment);
            check(Files.size(segment) > 0, "segment file is written");
            check(loaded.documentCount() == index.documentCount(), "document count round-trips");
            check(loaded.postings("search").size() == index.postings("search").size(), "postings round-trip");
            check(loaded.postings("search").get(0).getPositions().equals(index.postings("search").get(0).getPositions()), "positions round-trip");
            check(loaded.document(index.documents().get(0).getDocument().getDocumentId()) != null, "document metadata round-trips");
            System.out.println("IndexSegmentPersistenceTest: PASS");
        } finally {
            Files.deleteIfExists(segment);
            Files.deleteIfExists(directory);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
