package com.nebula.evaluation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** One labelled query with graded relevance judgments by source path. */
public final class EvaluationQuery {
    private final String queryId;
    private final String text;
    private final Map<String, Integer> relevanceBySourcePath;

    public EvaluationQuery(String queryId, String text, Map<String, Integer> relevanceBySourcePath) {
        this.queryId = queryId;
        this.text = text;
        this.relevanceBySourcePath = Collections.unmodifiableMap(new LinkedHashMap<>(relevanceBySourcePath));
    }

    public String getQueryId() { return queryId; }
    public String getText() { return text; }
    public Map<String, Integer> getRelevanceBySourcePath() { return relevanceBySourcePath; }
}
