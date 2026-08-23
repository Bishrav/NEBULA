package com.nebula.search;

import com.nebula.ingestion.DocumentIngestor;

/** Deterministic metadata-conflict fixture; this is sensitivity coverage, not a bias estimate. */
public final class TrustRobustnessFixtureTest {
    public static void main(String[] args) {
        long now = 1_700_000_000_000L;
        TrustMetadataStore metadata = new TrustMetadataStore();
        metadata.register(new DocumentTrustMetadata("docs/official-stale.md", 0.90, now - 120L * 86_400_000L, "official", "stale"));
        metadata.register(new DocumentTrustMetadata("docs/fresh-weak.md", 0.10, now, "community", "fresh"));
        InvertedIndex index = new InvertedIndex();
        DocumentIngestor ingestor = new DocumentIngestor();
        index.add(ingestor.ingest("docs/official-stale.md", "# Replication\n\nConfigure streaming replication for failover."));
        index.add(ingestor.ingest("docs/fresh-weak.md", "# Replication\n\nConfigure streaming replication for failover."));
        TrustAwareSearchEngine engine = new TrustAwareSearchEngine(new BM25SearchEngine(index), metadata,
                new FreshnessScorer(30.0), 0.30, 0.50, 0.20);
        check(engine.search("streaming replication", 2, now).get(0).getDocument().getSourcePath().equals("docs/official-stale.md"),
                "authority can overcome staleness in the conflict fixture");
        metadata.register(new DocumentTrustMetadata("docs/fresh-weak.md", 1.0, now, "manipulated", "verified"));
        check(engine.search("streaming replication", 2, now).get(0).getDocument().getSourcePath().equals("docs/fresh-weak.md"),
                "metadata manipulation changes the ranking");
        System.out.println("TrustRobustnessFixtureTest: PASS");
    }

    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
