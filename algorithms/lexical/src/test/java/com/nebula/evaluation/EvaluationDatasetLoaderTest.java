package com.nebula.evaluation;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class EvaluationDatasetLoaderTest {
    public static void main(String[] args) throws Exception {
        Path file = Files.createTempFile("evaluation-loader", ".psv");
        try {
            Files.writeString(file, "query_id|query|judgments\nq1|How do || operators work?|docs.md:3\n", StandardCharsets.UTF_8);
            List<EvaluationQuery> queries = new EvaluationDatasetLoader().load(file);
            check(queries.size() == 1, "one query loaded");
            check(queries.get(0).getText().equals("How do || operators work?"), "pipe-containing query preserved");
            check(queries.get(0).getRelevanceBySourcePath().get("docs.md") == 3, "judgement parsed");
            System.out.println("EvaluationDatasetLoaderTest: PASS");
        } finally {
            Files.deleteIfExists(file);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
