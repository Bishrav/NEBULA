package com.nebula.search;

import com.nebula.ingestion.DocumentIngestor;
import java.nio.file.Files;
import java.nio.file.Path;

/** Verifies that several persisted segments become one searchable index. */
public final class IndexSegmentLoaderTest {
    public static void main(String[] args) throws Exception {
        DocumentIngestor ingestor = new DocumentIngestor();
        InvertedIndex first = new InvertedIndex();
        first.add(ingestor.ingest("docs/a.md", "# Routing\n\nRouting uses a priority queue."));
        InvertedIndex second = new InvertedIndex();
        second.add(ingestor.ingest("docs/b.md", "# Sharding\n\nSharding uses consistent hashing."));

        Path directory = Files.createTempDirectory("nebula-segments-");
        Path firstPath = directory.resolve("segment-000001.idx");
        Path secondPath = directory.resolve("segment-000002.idx");
        try {
            IndexSegmentWriter writer = new IndexSegmentWriter();
            writer.write(firstPath, first);
            writer.write(secondPath, second);
            InvertedIndex loaded = new IndexSegmentLoader().loadDirectory(directory);
            check(loaded.documentCount() == 2, "both segments are loaded");
            check(new BM25SearchEngine(loaded).search("consistent hashing", 10).size() == 1, "loaded index is searchable");
            check(new IndexSegmentLoader().load(firstPath, firstPath).documentCount() == 1, "duplicate segment documents are idempotent");
            System.out.println("IndexSegmentLoaderTest: PASS");
        } finally {
            Files.deleteIfExists(firstPath);
            Files.deleteIfExists(secondPath);
            Files.deleteIfExists(directory);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
