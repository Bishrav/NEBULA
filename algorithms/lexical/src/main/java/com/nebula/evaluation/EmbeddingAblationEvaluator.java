package com.nebula.evaluation;

import com.nebula.ingestion.DocumentRecord;
import com.nebula.search.CharacterNgramEmbeddingModel;
import com.nebula.search.EmbeddingModel;
import com.nebula.search.HashingEmbeddingModel;
import com.nebula.search.SearchCatalog;
import com.nebula.search.SearchResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Runs a fixed-corpus ablation over deterministic embedding models. */
public final class EmbeddingAblationEvaluator {
    private static final int DEFAULT_DIMENSION = 128;

    public EmbeddingAblationReport evaluate(List<DocumentRecord> documents, List<EvaluationQuery> queries,
                                            int cutoff) {
        if (documents == null || queries == null) throw new IllegalArgumentException("documents and queries are required");
        if (cutoff <= 0) throw new IllegalArgumentException("cutoff must be positive");
        Map<String, EvaluationReport> reports = new LinkedHashMap<>();
        addModelReports(reports, "hashing", new HashingEmbeddingModel(DEFAULT_DIMENSION), documents, queries, cutoff);
        addModelReports(reports, "char_ngram", new CharacterNgramEmbeddingModel(DEFAULT_DIMENSION), documents, queries, cutoff);
        return new EmbeddingAblationReport(reports);
    }

    private void addModelReports(Map<String, EvaluationReport> reports, String name, EmbeddingModel model,
                                 List<DocumentRecord> documents, List<EvaluationQuery> queries, int cutoff) {
        SearchCatalog catalog = new SearchCatalog(new com.nebula.ingestion.DocumentIngestor(),
                new com.nebula.search.InvertedIndex(), model);
        for (DocumentRecord document : documents) catalog.indexMarkdown(document.getSourcePath(), document.getText());
        reports.put(name + ":semantic", evaluateSearch(catalog, queries, cutoff, false));
        reports.put(name + ":hybrid", evaluateSearch(catalog, queries, cutoff, true));
    }

    private EvaluationReport evaluateSearch(SearchCatalog catalog, List<EvaluationQuery> queries,
                                            int cutoff, boolean hybrid) {
        List<EvaluationMetrics> metrics = new ArrayList<>();
        for (EvaluationQuery query : queries) {
            List<SearchResult> results = hybrid
                    ? catalog.hybridSearch(query.getText(), cutoff)
                    : catalog.semanticSearch(query.getText(), cutoff);
            metrics.add(EvaluationMetricCalculator.calculate(query, results, cutoff));
        }
        return new EvaluationReport(cutoff, metrics);
    }
}
