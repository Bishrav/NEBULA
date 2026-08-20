package com.nebula.evaluation;

import com.nebula.search.SearchCatalog;
import java.util.ArrayList;
import java.util.List;

/** Evaluates a SearchCatalog against fixed graded source-path judgments. */
public final class SearchEvaluator {
    public EvaluationReport evaluate(SearchCatalog catalog, List<EvaluationQuery> queries, int cutoff) {
        if (catalog == null || queries == null) throw new IllegalArgumentException("catalog and queries are required");
        if (cutoff <= 0) throw new IllegalArgumentException("cutoff must be positive");
        List<EvaluationMetrics> metrics = new ArrayList<>();
        for (EvaluationQuery query : queries) {
            metrics.add(EvaluationMetricCalculator.calculate(query, catalog.search(query.getText(), cutoff), cutoff));
        }
        return new EvaluationReport(cutoff, metrics);
    }
}
