package com.nebula.evaluation;

import com.nebula.search.SearchCatalog;
import com.nebula.search.SearchResult;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Runs a controlled ranking ablation over baseline and trust variants. */
public final class RankingVariantEvaluator {
    public RankingComparisonReport evaluate(SearchCatalog catalog, List<EvaluationQuery> queries,
                                            int cutoff, long nowEpochMillis) {
        if (catalog == null || queries == null) throw new IllegalArgumentException("catalog and queries are required");
        if (cutoff <= 0) throw new IllegalArgumentException("cutoff must be positive");
        Map<String, EvaluationReport> reports = new LinkedHashMap<>();
        reports.put("bm25", evaluateVariant(catalog, queries, cutoff, nowEpochMillis, "bm25"));
        reports.put("authority_only", evaluateVariant(catalog, queries, cutoff, nowEpochMillis, "authority_only"));
        reports.put("freshness_only", evaluateVariant(catalog, queries, cutoff, nowEpochMillis, "freshness_only"));
        reports.put("trust_aware", evaluateVariant(catalog, queries, cutoff, nowEpochMillis, "trust_aware"));
        return new RankingComparisonReport(reports);
    }

    private EvaluationReport evaluateVariant(SearchCatalog catalog, List<EvaluationQuery> queries,
                                              int cutoff, long nowEpochMillis, String variant) {
        java.util.ArrayList<EvaluationMetrics> metrics = new java.util.ArrayList<>();
        for (EvaluationQuery query : queries) {
            List<SearchResult> results;
            if ("bm25".equals(variant)) results = catalog.search(query.getText(), cutoff);
            else if ("authority_only".equals(variant)) {
                results = catalog.searchTrustAware(query.getText(), cutoff, nowEpochMillis, 0.0, 1.0, 0.0);
            } else if ("freshness_only".equals(variant)) {
                results = catalog.searchTrustAware(query.getText(), cutoff, nowEpochMillis, 0.0, 0.0, 1.0);
            } else {
                results = catalog.searchTrustAware(query.getText(), cutoff, nowEpochMillis, 0.70, 0.20, 0.10);
            }
            metrics.add(EvaluationMetricCalculator.calculate(query, results, cutoff));
        }
        return new EvaluationReport(cutoff, metrics);
    }
}
