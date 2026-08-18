package com.nebula.ingestion;

import java.util.Optional;
import java.util.UUID;

/** Coordinates normalization, idempotency, versioning, and job results. */
public final class IngestionService {
    private final DocumentIngestor ingestor;
    private final DocumentMetadataRepository repository;

    public IngestionService(DocumentIngestor ingestor, DocumentMetadataRepository repository) {
        this.ingestor = ingestor;
        this.repository = repository;
    }

    public IngestionJob ingest(String sourcePath, String content) {
        String jobId = UUID.randomUUID().toString();
        try {
            DocumentRecord document = ingestor.ingest(sourcePath, content);
            VersionedDocument saved = repository.saveIfChanged(document);
            return IngestionJob.completed(jobId, saved.getDocument(), saved.getVersion());
        } catch (RuntimeException exception) {
            return IngestionJob.failed(jobId, sourcePath, exception.getMessage());
        }
    }

    public Optional<VersionedDocument> findDocument(String documentId) {
        return repository.findById(documentId);
    }

    public Optional<VersionedDocument> findCurrent(String sourcePath) {
        return repository.findCurrent(sourcePath);
    }
}
