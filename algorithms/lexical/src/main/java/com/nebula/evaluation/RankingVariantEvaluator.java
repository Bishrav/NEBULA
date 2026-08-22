package com.nebula.evaluation;

import com.nebula.search.SearchCatalog;
import com.nebula.search.SearchResult;

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
        reports.put("semantic", evaluateVariant(catalog, queries, cutoff, nowEpochMillis, "semantic"));
        reports.put("hybrid", evaluateVariant(catalog, queries, cutoff, nowEpochMillis, "hybrid"));
        reports.put("rrf", evaluateVariant(catalog, queries, cutoff, nowEpochMillis, "rrf"));
        reports.put("authority_only", evaluateVariant(catalog, queries, cutoff, nowEpochMillis, "authority_only"));
        reports.put("freshness_only", evaluateVariant(catalog, queries, cutoff, nowEpochMillis, "freshness_only"));
        reports.put("trust_aware", evaluateVariant(catalog, queries, cutoff, nowEpochMillis, "trust_aware"));
        reports.put("a3_hybrid_freshness", evaluateVariant(catalog, queries, cutoff, nowEpochMillis, "a3_hybrid_freshness"));
        reports.put("a4_hybrid_authority", evaluateVariant(catalog, queries, cutoff, nowEpochMillis, "a4_hybrid_authority"));
        reports.put("a5_hybrid_graph", evaluateVariant(catalog, queries, cutoff, nowEpochMillis, "a5_hybrid_graph"));
        reports.put("a6_hybrid_freshness_authority", evaluateVariant(catalog, queries, cutoff, nowEpochMillis, "a6_hybrid_freshness_authority"));
        reports.put("a7_hybrid_authority_graph", evaluateVariant(catalog, queries, cutoff, nowEpochMillis, "a7_hybrid_authority_graph"));
        reports.put("a8_hybrid_freshness_graph", evaluateVariant(catalog, queries, cutoff, nowEpochMillis, "a8_hybrid_freshness_graph"));
        reports.put("a9_full_nebula", evaluateVariant(catalog, queries, cutoff, nowEpochMillis, "a9_full_nebula"));
        return new RankingComparisonReport(reports);
    }

    private EvaluationReport evaluateVariant(SearchCatalog catalog, List<EvaluationQuery> queries,
                                              int cutoff, long nowEpochMillis, String variant) {
        java.util.ArrayList<EvaluationMetrics> metrics = new java.util.ArrayList<>();
        for (EvaluationQuery query : queries) {
            List<SearchResult> results = searchVariant(catalog, query, cutoff, nowEpochMillis, variant);
            metrics.add(EvaluationMetricCalculator.calculate(query, results, cutoff));
        }
        return new EvaluationReport(cutoff, metrics);
    }

    public List<SearchResult> searchVariant(SearchCatalog catalog, EvaluationQuery query,
                                            int cutoff, long nowEpochMillis, String variant) {
        if ("bm25".equals(variant)) return catalog.search(query.getText(), cutoff);
        if ("semantic".equals(variant)) return catalog.semanticSearch(query.getText(), cutoff);
        if ("hybrid".equals(variant)) return catalog.hybridSearch(query.getText(), cutoff);
        if ("rrf".equals(variant)) return catalog.rrfSearch(query.getText(), cutoff);
        if ("authority_only".equals(variant)) {
            return catalog.searchTrustAware(query.getText(), cutoff, nowEpochMillis, 0.0, 1.0, 0.0);
        }
        if ("freshness_only".equals(variant)) {
            return catalog.searchTrustAware(query.getText(), cutoff, nowEpochMillis, 0.0, 0.0, 1.0);
        }
        if ("trust_aware".equals(variant)) {
            return catalog.searchTrustAware(query.getText(), cutoff, nowEpochMillis, 0.70, 0.20, 0.10);
        }
        if ("a3_hybrid_freshness".equals(variant)) return catalog.searchTrustOrientedHybrid(query.getText(), cutoff, nowEpochMillis, 0.45, 0.45, 0.0, 0.10, 0.0);
        if ("a4_hybrid_authority".equals(variant)) return catalog.searchTrustOrientedHybrid(query.getText(), cutoff, nowEpochMillis, 0.45, 0.45, 0.10, 0.0, 0.0);
        if ("a5_hybrid_graph".equals(variant)) return catalog.searchTrustOrientedHybrid(query.getText(), cutoff, nowEpochMillis, 0.45, 0.45, 0.0, 0.0, 0.10);
        if ("a6_hybrid_freshness_authority".equals(variant)) return catalog.searchTrustOrientedHybrid(query.getText(), cutoff, nowEpochMillis, 0.40, 0.40, 0.10, 0.10, 0.0);
        if ("a7_hybrid_authority_graph".equals(variant)) return catalog.searchTrustOrientedHybrid(query.getText(), cutoff, nowEpochMillis, 0.40, 0.40, 0.10, 0.0, 0.10);
        if ("a8_hybrid_freshness_graph".equals(variant)) return catalog.searchTrustOrientedHybrid(query.getText(), cutoff, nowEpochMillis, 0.40, 0.40, 0.0, 0.10, 0.10);
        if ("a9_full_nebula".equals(variant)) return catalog.searchTrustOrientedHybrid(query.getText(), cutoff, nowEpochMillis, 0.35, 0.35, 0.10, 0.10, 0.10);
        throw new IllegalArgumentException("unknown ranking variant: " + variant);
    }
}
