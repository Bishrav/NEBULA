package com.nebula.search;

import java.util.List;

/** Tests freshness decay, authority-aware reranking, and trust explanations. */
public final class TrustAwareSearchEngineTest {
    public static void main(String[] args) {
        TrustMetadataStore store = new TrustMetadataStore();
        long now = 1_700_000_000_000L;
        store.register(new DocumentTrustMetadata("docs/official.md", 0.2, now - 120L * 86_400_000L, "platform", "stale"));
        store.register(new DocumentTrustMetadata("docs/notes.md", 1.0, now, "security", "verified"));
        InvertedIndex index = new InvertedIndex();
        com.nebula.ingestion.DocumentIngestor ingestor = new com.nebula.ingestion.DocumentIngestor();
        index.add(ingestor.ingest("docs/official.md", "# Incident Response\n\nIncident response guidance for production services."));
        index.add(ingestor.ingest("docs/notes.md", "# Incident Response Notes\n\nIncident response notes for the team."));
        TrustAwareSearchEngine engine = new TrustAwareSearchEngine(new BM25SearchEngine(index), store);

        List<SearchResult> results = engine.search("incident response", 2, now);
        check(results.size() == 2, "trust search returns candidates");
        check(results.get(0).getDocument().getSourcePath().equals("docs/notes.md"), "authority and freshness rerank results");
        check(results.get(0).getTermContributions().containsKey("signal:freshness"), "freshness is explained");
        check(new FreshnessScorer(30.0).score(now - 30L * 86_400_000L, now) > 0.49, "half-life decay is calculated");
        System.out.println("TrustAwareSearchEngineTest: PASS");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
