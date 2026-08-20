package com.nebula.evaluation;

import com.nebula.ingestion.DocumentRecord;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Regression test for exact-vs-HNSW benchmark accounting. */
public final class AnnBenchmarkTest {
    public static void main(String[] args) {
        List<DocumentRecord> documents = Arrays.asList(
                new DocumentRecord("a", "routing.md", "markdown", "Routing", "Gateway routes requests to services.", "hash-a"),
                new DocumentRecord("b", "storage.md", "markdown", "Storage", "Storage keeps durable indexes.", "hash-b"),
                new DocumentRecord("c", "deploy.md", "markdown", "Deploy", "Deployment publishes the gateway.", "hash-c"));
        EvaluationQuery query = new EvaluationQuery("q1", "gateway routes", Collections.singletonMap("routing.md", 3));
        AnnBenchmark.Result result = new AnnBenchmark().evaluate(documents, Arrays.asList(query), 2);
        check(result.getDocuments() == 3, "document count recorded");
        check(result.getQueries() == 1, "query count recorded");
        check(result.getRecall() >= 0.0 && result.getRecall() <= 1.0, "recall is bounded");
        System.out.println("AnnBenchmarkTest: PASS");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
