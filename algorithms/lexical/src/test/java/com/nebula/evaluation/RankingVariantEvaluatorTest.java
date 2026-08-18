package com.nebula.evaluation;

import com.nebula.search.DocumentTrustMetadata;
import com.nebula.search.SearchCatalog;

import java.util.Collections;

/** Tests the baseline/trust ablation contract and metric deltas. */
public final class RankingVariantEvaluatorTest {
    public static void main(String[] args) {
        SearchCatalog catalog = new SearchCatalog();
        catalog.indexMarkdown("docs/official.md", "# Incident Response\n\nIncident response guidance for production services.");
        catalog.indexMarkdown("docs/notes.md", "# Incident Response Notes\n\nIncident response notes for the team.");
        long now = 1_700_000_000_000L;
        catalog.registerTrustMetadata(new DocumentTrustMetadata("docs/official.md", 0.2, now - 120L * 86_400_000L, "platform", "stale"));
        catalog.registerTrustMetadata(new DocumentTrustMetadata("docs/notes.md", 1.0, now, "security", "verified"));

        EvaluationQuery query = new EvaluationQuery("q1", "incident response", Collections.singletonMap("docs/notes.md", 3));
        RankingComparisonReport report = new RankingVariantEvaluator().evaluate(
                catalog, Collections.singletonList(query), 2, now);
        check(report.getVariants().size() == 5, "five ranking variants are evaluated");
        check(report.get("hybrid") != null, "hybrid report exists");
        check(report.get("bm25") != null && report.get("trust_aware") != null, "baseline and trust reports exist");
        check(report.ndcgDelta("trust_aware", "bm25") >= 0.0, "trust delta is calculable");
        System.out.println("RankingVariantEvaluatorTest: PASS");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
