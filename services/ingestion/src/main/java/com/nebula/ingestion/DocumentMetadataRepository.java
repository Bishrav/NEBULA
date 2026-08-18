package com.nebula.ingestion;

import java.util.List;
import java.util.Optional;

public interface DocumentMetadataRepository {
    VersionedDocument saveIfChanged(DocumentRecord document);
    Optional<VersionedDocument> findCurrent(String sourcePath);
    Optional<VersionedDocument> findById(String documentId);
    List<VersionedDocument> findVersions(String sourcePath);
}
