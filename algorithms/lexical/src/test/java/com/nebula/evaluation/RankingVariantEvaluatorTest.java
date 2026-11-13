package com.nebula.evaluation;

import com.nebula.search.DocumentTrustMetadata;
import com.nebula.search.CachedEmbeddingModel;
import com.nebula.search.InvertedIndex;
import com.nebula.search.SearchCatalog;
import com.nebula.ingestion.DocumentIngestor;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

/** Tests the baseline/trust ablation contract and metric deltas. */
public final class RankingVariantEvaluatorTest {
    public static void main(String[] args) throws Exception {
        String officialSource = "# Incident Response\n\nIncident response guidance for production services.";
        String notesSource = "# Incident Response Notes\n\nIncident response notes for the team.";
        String queryText = "incident response";
        DocumentIngestor ingestor = new DocumentIngestor();
        String official = ingestor.ingest("docs/official.md", officialSource).getText();
        String notes = ingestor.ingest("docs/notes.md", notesSource).getText();
        Path cache = Files.createTempFile("nebula-ranking-variant", ".tsv");
        String vector = "1.0,0.0,0.0";
        Files.writeString(cache,
                "# modelId=BAAI/bge-base-en-v1.5\n"
                        + "# revision=a5beb1e3e68b9ab74eb54cfd186867f64f240e1a\n"
                        + "# license=MIT\n# dimension=3\n# normalized=true\n"
                        + CachedEmbeddingModel.hash(official) + "\t" + vector + "\n"
                        + CachedEmbeddingModel.hash(notes) + "\t" + vector + "\n"
                        + CachedEmbeddingModel.hash(queryText) + "\t" + vector + "\n",
                StandardCharsets.UTF_8);
        SearchCatalog catalog = new SearchCatalog(ingestor, new InvertedIndex(),
                new CachedEmbeddingModel(cache));
        catalog.indexMarkdown("docs/official.md", officialSource);
        catalog.indexMarkdown("docs/notes.md", notesSource);
        long now = 1_700_000_000_000L;
        catalog.registerTrustMetadata(new DocumentTrustMetadata("docs/official.md", 0.2, now - 120L * 86_400_000L, "platform", "stale"));
        catalog.registerTrustMetadata(new DocumentTrustMetadata("docs/notes.md", 1.0, now, "security", "verified"));

        EvaluationQuery query = new EvaluationQuery("q1", "incident response", Collections.singletonMap("docs/notes.md", 3));
        RankingComparisonReport report = new RankingVariantEvaluator().evaluate(
                catalog, Collections.singletonList(query), 2, now);
        check(report.getVariants().size() == 15, "legacy baselines, BGE hybrid, and A0-A9 variants are evaluated");
        check(report.get("semantic") != null && report.get("hybrid") != null, "semantic and hybrid reports exist");
        check(report.get("bm25") != null && report.get("trust_aware") != null, "baseline and trust reports exist");
        check(report.ndcgDelta("trust_aware", "bm25") >= 0.0, "trust delta is calculable");
        System.out.println("RankingVariantEvaluatorTest: PASS");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
