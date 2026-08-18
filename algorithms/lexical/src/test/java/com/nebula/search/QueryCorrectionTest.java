package com.nebula.search;

import java.util.List;

/** Tests deterministic recovery from one-character technical term typos. */
public final class QueryCorrectionTest {
    public static void main(String[] args) {
        SearchCatalog catalog = new SearchCatalog();
        catalog.indexMarkdown("docs/operations.md", "# Incident Response\n\nWhen a shard fails, use a replica and report a partial result.");

        List<SearchResult> results = catalog.search("share failure", 5);
        check(results.size() == 1, "one-character typo still retrieves the document");
        check(results.get(0).getDocument().getSourcePath().equals("docs/operations.md"), "corrected result is returned");
        check(results.get(0).getTermContributions().containsKey("correction:share->shard"),
                "correction is exposed in the explanation");
        System.out.println("QueryCorrectionTest: PASS");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
