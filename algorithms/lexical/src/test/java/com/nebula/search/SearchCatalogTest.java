package com.nebula.search;

import java.util.List;

/** Tests the application boundary between ingestion and lexical retrieval. */
public final class SearchCatalogTest {
    public static void main(String[] args) {
        SearchCatalog catalog = new SearchCatalog();
        catalog.indexMarkdown("docs/architecture.md", "# Architecture\n\nThe query coordinator merges shard results.");
        catalog.indexMarkdown("docs/runbook.md", "# Runbook\n\nThe deployment runbook checks service health.");

        List<SearchResult> results = catalog.search("query coordinator", 5);
        check(catalog.documentCount() == 2, "documents are indexed");
        check(results.size() == 1, "matching documents are returned");
        check(results.get(0).getDocument().getTitle().equals("Architecture"), "matching title is returned");
        System.out.println("SearchCatalogTest: PASS");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
