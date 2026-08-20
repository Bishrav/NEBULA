package com.nebula.evaluation;

import com.nebula.search.SearchResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Shared metric calculation for baseline and alternative ranking variants. */
public final class EvaluationMetricCalculator {
    private EvaluationMetricCalculator() { }

    public static EvaluationMetrics calculate(EvaluationQuery query, List<SearchResult> results, int cutoff) {
        Map<String, Integer> judgments = query.getRelevanceBySourcePath();
        int relevantRetrieved = 0;
        double reciprocalRank = 0.0;
        double dcg = 0.0;
        for (int index = 0; index < results.size(); index++) {
            int grade = gradeOf(results.get(index), judgments);
            if (grade > 0) {
                relevantRetrieved++;
                if (reciprocalRank == 0.0) reciprocalRank = 1.0 / (index + 1);
            }
            dcg += gain(grade) / log2(index + 2);
        }
        double precision = (double) relevantRetrieved / cutoff;
        int totalRelevant = 0;
        for (Integer grade : judgments.values()) if (grade != null && grade > 0) totalRelevant++;
        double recall = totalRelevant == 0 ? 0.0 : (double) relevantRetrieved / totalRelevant;
        List<Integer> idealGrades = new ArrayList<>(judgments.values());
        Collections.sort(idealGrades, Collections.reverseOrder());
        double idcg = 0.0;
        for (int index = 0; index < Math.min(cutoff, idealGrades.size()); index++) {
            idcg += gain(idealGrades.get(index)) / log2(index + 2);
        }
        double ndcg = idcg == 0.0 ? 0.0 : dcg / idcg;
        return new EvaluationMetrics(query.getQueryId(), precision, recall, reciprocalRank, ndcg);
    }

    private static int gradeOf(SearchResult result, Map<String, Integer> judgments) {
        Integer grade = judgments.get(result.getDocument().getSourcePath());
        return grade == null ? 0 : grade;
    }

    private static double gain(int grade) { return Math.pow(2.0, grade) - 1.0; }
    private static double log2(int value) { return Math.log(value) / Math.log(2.0); }
}
