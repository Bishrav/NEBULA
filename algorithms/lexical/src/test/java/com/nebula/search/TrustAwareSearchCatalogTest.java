package com.nebula.search;

import java.util.List;

/** Tests trust-aware ranking through the SearchCatalog application boundary. */
public final class TrustAwareSearchCatalogTest {
    public static void main(String[] args) {
        SearchCatalog catalog = new SearchCatalog();
        catalog.indexMarkdown("docs/official.md", "# Incident Response\n\nIncident response guidance for production services.");
        catalog.indexMarkdown("docs/notes.md", "# Incident Response Notes\n\nIncident response notes for the team.");
        long now = 1_700_000_000_000L;
        catalog.registerTrustMetadata(new DocumentTrustMetadata("docs/official.md", 0.2,
                now - 120L * 86_400_000L, "platform", "stale"));
        catalog.registerTrustMetadata(new DocumentTrustMetadata("docs/notes.md", 1.0,
                now, "security", "verified"));

        List<SearchResult> baseline = catalog.search("incident response", 2);
        List<SearchResult> trustAware = catalog.searchTrustAware("incident response", 2, now);
        check(baseline.size() == 2 && trustAware.size() == 2, "both ranking modes return results");
        check(trustAware.get(0).getDocument().getSourcePath().equals("docs/notes.md"), "trust-aware mode reranks results");
        check(trustAware.get(0).getTermContributions().containsKey("signal:authority"), "trust signals are exposed");
        System.out.println("TrustAwareSearchCatalogTest: PASS");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
