package com.nebula.search;

import java.util.List;

/** Verifies that ingested links create graph authority available to trust-aware search. */
public final class PageRankTrustIntegrationTest {
    public static void main(String[] args) {
        SearchCatalog catalog = new SearchCatalog();
        catalog.indexMarkdown("a.md", "# Search A\n\nSearch concepts. [B](b.md)");
        catalog.indexMarkdown("b.md", "# Search B\n\nSearch concepts. [B](b.md) [C](c.md)");
        catalog.indexMarkdown("c.md", "# Search C\n\nSearch concepts.");
        long now = 1_700_000_000_000L;
        List<SearchResult> results = catalog.searchTrustAware("search concepts", 3, now);
        check(catalog.pageRank().score("b.md") > catalog.pageRank().score("a.md"), "PageRank detects linked authority");
        check(results.get(0).getTermContributions().containsKey("signal:pagerank"), "PageRank is explained");
        System.out.println("PageRankTrustIntegrationTest: PASS");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
