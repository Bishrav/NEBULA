package com.nebula.ingestion;

import java.util.Arrays;

/** Tests relative Markdown link extraction and external-link filtering. */
public final class DocumentLinkExtractionTest {
    public static void main(String[] args) {
        DocumentRecord document = new DocumentIngestor().ingest("docs/architecture.md",
                "# Architecture\n\n[Runbook](runbook.md) [Details](../details.md#section) [External](https://example.com).");
        check(document.getLinks().equals(Arrays.asList("docs/runbook.md", "details.md")), "relative links are normalized");
        check(!document.getLinks().contains("https://example.com"), "external links are excluded");
        System.out.println("DocumentLinkExtractionTest: PASS");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
