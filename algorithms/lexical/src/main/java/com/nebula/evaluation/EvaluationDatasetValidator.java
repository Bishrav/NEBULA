package com.nebula.evaluation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Validates benchmark labels before they are used to make ranking claims. */
public final class EvaluationDatasetValidator {
    private static final int MAX_RELEVANCE_GRADE = 3;

    public EvaluationDatasetValidationReport validate(List<EvaluationQuery> queries,
                                                      Set<String> corpusSourcePaths) {
        if (queries == null || corpusSourcePaths == null) {
            throw new IllegalArgumentException("queries and corpus source paths are required");
        }
        Set<String> queryIds = new HashSet<>();
        int judgementCount = 0;
        int relevantJudgementCount = 0;
        for (EvaluationQuery query : queries) {
            if (query == null || blank(query.getQueryId()) || blank(query.getText())) {
                throw new IllegalArgumentException("query id and text must not be blank");
            }
            if (!queryIds.add(query.getQueryId())) {
                throw new IllegalArgumentException("duplicate query id: " + query.getQueryId());
            }
            if (query.getRelevanceBySourcePath().isEmpty()) {
                throw new IllegalArgumentException("query has no judgements: " + query.getQueryId());
            }
            for (java.util.Map.Entry<String, Integer> judgement : query.getRelevanceBySourcePath().entrySet()) {
                String sourcePath = judgement.getKey();
                Integer grade = judgement.getValue();
                if (blank(sourcePath) || !corpusSourcePaths.contains(sourcePath)) {
                    throw new IllegalArgumentException("judgement references unknown source: " + sourcePath);
                }
                if (grade == null || grade < 0 || grade > MAX_RELEVANCE_GRADE) {
                    throw new IllegalArgumentException("relevance grade must be between 0 and 3: " + grade);
                }
                judgementCount++;
                if (grade > 0) relevantJudgementCount++;
            }
            if (!hasRelevantJudgement(query)) {
                throw new IllegalArgumentException("query has no positive judgement: " + query.getQueryId());
            }
        }
        if (queries.isEmpty()) throw new IllegalArgumentException("evaluation dataset must contain queries");
        return new EvaluationDatasetValidationReport(queries.size(), judgementCount, relevantJudgementCount);
    }

    private boolean hasRelevantJudgement(EvaluationQuery query) {
        for (Integer grade : query.getRelevanceBySourcePath().values()) {
            if (grade != null && grade > 0) return true;
        }
        return false;
    }

    private static boolean blank(String value) { return value == null || value.trim().isEmpty(); }
}
