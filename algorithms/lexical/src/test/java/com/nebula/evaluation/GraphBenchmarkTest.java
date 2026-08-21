package com.nebula.evaluation;

import com.nebula.ingestion.DocumentIngestor;
import com.nebula.ingestion.DocumentRecord;
import java.util.Arrays;
import java.util.List;

/** Regression test for deterministic graph health measurements. */
public final class GraphBenchmarkTest {
    public static void main(String[] args) {
        DocumentIngestor ingestor = new DocumentIngestor();
        List<DocumentRecord> documents = Arrays.asList(
                ingestor.ingest("a.md", "# A\n\n[B](b.md)"),
                ingestor.ingest("b.md", "# B\n\n[C](c.md)"),
                ingestor.ingest("c.md", "# C\n\n"));
        GraphBenchmark.Result result = new GraphBenchmark().evaluate(documents);
        check(result.getNodes() == 3, "graph nodes are measured");
        check(result.getEdges() == 2, "graph edges are measured");
        check(result.getDangling() == 1, "dangling node is measured");
        check(result.isConverged(), "PageRank converges");
        check(Math.abs(result.getScoreMass() - 1.0) < 0.000001, "PageRank score mass is conserved");
        System.out.println("GraphBenchmarkTest: PASS");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
