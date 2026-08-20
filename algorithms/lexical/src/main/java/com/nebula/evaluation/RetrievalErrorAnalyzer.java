package com.nebula.evaluation;

import com.nebula.search.SearchResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Identifies missed relevant sources and unexpected retrieved sources. */
public final class RetrievalErrorAnalyzer {
    public QueryErrorAnalysis analyze(String variant, EvaluationQuery query, List<SearchResult> results) {
        Map<String, Integer> ranks = new LinkedHashMap<>();
        List<String> unexpected = new ArrayList<>();
        for (int index = 0; index < results.size(); index++) {
            String sourcePath = results.get(index).getDocument().getSourcePath();
            ranks.put(sourcePath, index + 1);
            Integer grade = query.getRelevanceBySourcePath().get(sourcePath);
            if (grade == null || grade <= 0) unexpected.add(sourcePath);
        }
        List<String> missed = new ArrayList<>();
        for (Map.Entry<String, Integer> judgment : query.getRelevanceBySourcePath().entrySet()) {
            if (judgment.getValue() != null && judgment.getValue() > 0 && !ranks.containsKey(judgment.getKey())) {
                missed.add(judgment.getKey());
            }
        }
        return new QueryErrorAnalysis(variant, query.getQueryId(), missed, unexpected, ranks);
    }
}
