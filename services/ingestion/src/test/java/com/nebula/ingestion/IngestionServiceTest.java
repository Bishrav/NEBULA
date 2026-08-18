package com.nebula.ingestion;

/** Dependency-free tests for idempotency, versioning, and failure handling. */
public final class IngestionServiceTest {
    public static void main(String[] args) {
        InMemoryDocumentMetadataRepository repository = new InMemoryDocumentMetadataRepository();
        IngestionService service = new IngestionService(new DocumentIngestor(), repository);

        IngestionJob first = service.ingest("docs/runbook.md", "# Runbook\n\nRestart the service.");
        IngestionJob duplicate = service.ingest("docs/runbook.md", "# Runbook\n\nRestart the service.");
        IngestionJob changed = service.ingest("docs/runbook.md", "# Runbook\n\nRestart the service and verify health.");
        IngestionJob failed = service.ingest("docs/runbook.txt", "unsupported");

        check(first.getStatus() == IngestionStatus.COMPLETED, "first document completes");
        check(first.getVersion() == 1, "first document is version one");
        check(duplicate.getVersion() == 1, "duplicate does not create a version");
        check(changed.getVersion() == 2, "changed content creates version two");
        check(failed.getStatus() == IngestionStatus.FAILED, "unsupported content fails safely");
        check(repository.findVersions("docs/runbook.md").size() == 2, "two versions are retained");
        System.out.println("IngestionServiceTest: PASS");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
