package com.nebula.ingestion;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Dependency-free smoke tests; executable with the JDK when Maven is unavailable. */
public final class DocumentIngestorTest {
    public static void main(String[] args) throws Exception {
        Path file = Files.createTempFile("nebula-ingestion-", ".md");
        try {
            Files.write(file, ("# Deployment Runbook\n\n" +
                    "Use **blue-green** deployment. See [rollback](./rollback.md).\n" +
                    "\n- Verify health checks\n- Shift traffic\n").getBytes(StandardCharsets.UTF_8));

            DocumentIngestor ingestor = new DocumentIngestor();
            DocumentRecord first = ingestor.ingest(file);
            DocumentRecord second = ingestor.ingest(file);

            check("Deployment Runbook".equals(first.getTitle()), "extracts the H1 title");
            check(first.getText().contains("blue-green deployment"), "normalizes emphasis");
            check(first.getText().contains("rollback"), "keeps link labels");
            check(!first.getText().contains("**"), "removes Markdown emphasis markers");
            check(first.equals(second), "same input produces the same identity");
            check(first.getContentHash().length() == 64, "uses SHA-256 content hashes");
            System.out.println("DocumentIngestorTest: PASS");
        } finally {
            Files.deleteIfExists(file);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
