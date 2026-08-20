package com.nebula.evaluation;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

/** Tests benchmark label integrity rules. */
public final class EvaluationDatasetValidatorTest {
    public static void main(String[] args) {
        EvaluationQuery valid = new EvaluationQuery("q1", "routing",
                Collections.singletonMap("docs/routing.md", 3));
        EvaluationDatasetValidationReport report = new EvaluationDatasetValidator().validate(
                Collections.singletonList(valid), new HashSet<>(Collections.singletonList("docs/routing.md")));
        check(report.getQueryCount() == 1, "query count is reported");
        check(report.getRelevantJudgementCount() == 1, "positive judgement count is reported");

        expectFailure(Arrays.asList(valid, valid), "duplicate query ids are rejected");
        expectFailure(Collections.singletonList(new EvaluationQuery("q2", "routing",
                Collections.<String, Integer>emptyMap())), "empty judgements are rejected");
        expectFailure(Collections.singletonList(new EvaluationQuery("q3", "routing",
                Collections.singletonMap("docs/missing.md", 3))), "unknown sources are rejected");
        expectFailure(Collections.singletonList(new EvaluationQuery("q4", "routing",
                Collections.singletonMap("docs/routing.md", 0))), "queries need a positive judgement");
        System.out.println("EvaluationDatasetValidatorTest: PASS");
    }

    private static void expectFailure(java.util.List<EvaluationQuery> queries, String message) {
        try {
            new EvaluationDatasetValidator().validate(queries,
                    new HashSet<>(Collections.singletonList("docs/routing.md")));
            throw new AssertionError(message);
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
