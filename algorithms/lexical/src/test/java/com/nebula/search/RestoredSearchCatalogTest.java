package com.nebula.search;

import com.nebula.ingestion.DocumentIngestor;
import java.nio.file.Files;
import java.nio.file.Path;

/** Verifies that a new catalog can search persisted segments after a simulated restart. */
public final class RestoredSearchCatalogTest {
    public static void main(String[] args) throws Exception {
        Path directory = Files.createTempDirectory("nebula-restart-");
        try {
            InvertedIndex first = new InvertedIndex();
            first.add(new DocumentIngestor().ingest("docs/one.md", "# Recovery\n\nRestore the index after restart."));
            new IndexSegmentWriter().write(directory.resolve("segment-000001.idx"), first);

            SearchCatalog restored = SearchCatalog.fromSegmentDirectory(directory);
            check(restored.documentCount() == 1, "restored catalog sees persisted documents");
            check(restored.search("index restart", 10).size() == 1, "restored catalog is searchable");
            System.out.println("RestoredSearchCatalogTest: PASS");
        } finally {
            Files.deleteIfExists(directory.resolve("segment-000001.idx"));
            Files.deleteIfExists(directory);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
