package com.nebula.evaluation;

import com.nebula.search.SearchCatalog;
import java.util.Arrays;
import java.util.Collections;

/** Tests Precision@k, Recall@k, MRR, and NDCG on a controlled corpus. */
public final class SearchEvaluatorTest {
    public static void main(String[] args) {
        SearchCatalog catalog = new SearchCatalog();
        catalog.indexMarkdown("docs/primary.md", "# Distributed Search\n\nDistributed search uses a query coordinator.");
        catalog.indexMarkdown("docs/secondary.md", "# Search Notes\n\nSearch systems rank results.");

        EvaluationQuery query = new EvaluationQuery("q1", "distributed search", Collections.singletonMap("docs/primary.md", 3));
        EvaluationReport report = new SearchEvaluator().evaluate(catalog, Arrays.asList(query), 2);
        EvaluationMetrics metrics = report.getPerQuery().get(0);
        check(metrics.getPrecisionAtK() == 0.5, "precision at k is correct");
        check(metrics.getRecallAtK() == 1.0, "recall at k is correct");
        check(metrics.getReciprocalRank() == 1.0, "MRR is correct");
        check(metrics.getNdcgAtK() > 0.99, "NDCG rewards the ideal first result");
        System.out.println("SearchEvaluatorTest: PASS");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
