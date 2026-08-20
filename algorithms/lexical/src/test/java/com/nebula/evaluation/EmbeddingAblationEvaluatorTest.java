package com.nebula.evaluation;

import com.nebula.ingestion.DocumentIngestor;
import com.nebula.ingestion.DocumentRecord;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Tests fixed-corpus embedding ablation and report generation. */
public final class EmbeddingAblationEvaluatorTest {
    public static void main(String[] args) throws Exception {
        DocumentIngestor ingestor = new DocumentIngestor();
        List<DocumentRecord> documents = Arrays.asList(
                ingestor.ingest("docs/routing.md", "# Routing\n\nRoute requests through a gateway."),
                ingestor.ingest("docs/storage.md", "# Storage\n\nPersist objects in a database."));
        EvaluationQuery query = new EvaluationQuery("q1", "gateway routing",
                Collections.singletonMap("docs/routing.md", 3));
        EmbeddingAblationReport report = new EmbeddingAblationEvaluator()
                .evaluate(documents, Collections.singletonList(query), 2);
        check(report.getReports().size() == 4, "both models and retrieval modes are evaluated");
        check(report.get("hashing:semantic") != null, "hashing semantic report exists");
        check(report.get("char_ngram:hybrid") != null, "character n-gram hybrid report exists");
        java.nio.file.Path target = java.nio.file.Files.createTempFile("nebula-embedding-", ".md");
        try {
            new EmbeddingAblationReportWriter().write(target, documents.size(), 1, report);
            String content = new String(java.nio.file.Files.readAllBytes(target), java.nio.charset.StandardCharsets.UTF_8);
            check(content.contains("Embedding Ablation Report"), "ablation report has title");
            check(content.contains("char_ngram:hybrid"), "ablation report has all variants");
        } finally {
            java.nio.file.Files.deleteIfExists(target);
        }
        System.out.println("EmbeddingAblationEvaluatorTest: PASS");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
