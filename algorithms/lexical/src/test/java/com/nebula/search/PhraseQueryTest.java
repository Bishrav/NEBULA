package com.nebula.search;

import java.util.List;

/** Tests exact phrase matching and mixed term-plus-phrase queries. */
public final class PhraseQueryTest {
    public static void main(String[] args) {
        SearchCatalog catalog = new SearchCatalog();
        catalog.indexMarkdown("docs/exact.md", "# Exact\n\nThe query coordinator merges shard results.");
        catalog.indexMarkdown("docs/near.md", "# Near\n\nThe coordinator receives the query from shards.");

        List<SearchResult> exact = catalog.search("\"query coordinator\"", 10);
        List<SearchResult> mixed = catalog.search("distributed \"query coordinator\"", 10);
        check(exact.size() == 1, "only exact phrase matches");
        check(exact.get(0).getDocument().getTitle().equals("Exact"), "exact phrase document is returned");
        check(mixed.isEmpty(), "unmatched free term prevents a result");
        System.out.println("PhraseQueryTest: PASS");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
