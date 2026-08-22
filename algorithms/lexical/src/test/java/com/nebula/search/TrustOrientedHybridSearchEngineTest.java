package com.nebula.search;

import java.util.List;

/** Tests the full observable-signal hybrid ranking path. */
public final class TrustOrientedHybridSearchEngineTest {
    public static void main(String[] args) {
        SearchCatalog catalog = new SearchCatalog();
        long now = 1_700_000_000_000L;
        catalog.indexMarkdown("docs/canonical.md", "# Replication\n\nConfigure streaming replication and failover.");
        catalog.indexMarkdown("docs/new-note.md", "# Replication\n\nRecent notes about streaming replication.");
        catalog.registerTrustMetadata(new DocumentTrustMetadata("docs/canonical.md", 1.0, now - 90L * 86_400_000L, "maintainer", "verified"));
        catalog.registerTrustMetadata(new DocumentTrustMetadata("docs/new-note.md", 0.2, now, "unknown", "unverified"));
        List<SearchResult> results = catalog.searchTrustOrientedHybrid("configure streaming replication", 2, now,
                0.35, 0.35, 0.10, 0.10, 0.10);
        check(results.size() == 2, "full ranking returns results");
        check(results.get(0).getTermContributions().containsKey("signal:trust_oriented_hybrid"), "full explanation exists");
        check(results.get(0).getTermContributions().containsKey("signal:semantic"), "semantic contribution exists");
        System.out.println("TrustOrientedHybridSearchEngineTest: PASS");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
