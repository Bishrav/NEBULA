package com.nebula.search;

import java.util.List;

/** Tests normalized, explainable lexical-semantic score fusion. */
public final class HybridSearchEngineTest {
    public static void main(String[] args) {
        SearchCatalog catalog = new SearchCatalog();
        catalog.indexMarkdown("docs/routing.md", "# Routing\n\nRoute requests through a gateway.");
        catalog.indexMarkdown("docs/storage.md", "# Storage\n\nPersist objects in a database.");

        List<SearchResult> results = catalog.hybridSearch("gateway routes requests", 2);
        check(results.size() == 2, "hybrid retrieval returns top-k candidates");
        check(results.get(0).getDocument().getSourcePath().equals("docs/routing.md"),
                "hybrid retrieval preserves the strongest result");
        check(results.get(0).getTermContributions().containsKey("signal:lexical"),
                "lexical channel is explained");
        check(results.get(0).getTermContributions().containsKey("signal:semantic"),
                "semantic channel is explained");
        check(results.get(0).getTermContributions().containsKey("signal:hybrid"),
                "fused score is explained");
        System.out.println("HybridSearchEngineTest: PASS");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
