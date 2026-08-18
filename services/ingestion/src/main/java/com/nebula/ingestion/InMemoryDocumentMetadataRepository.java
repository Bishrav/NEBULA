package com.nebula.ingestion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** In-memory adapter used by local development and unit tests. */
public final class InMemoryDocumentMetadataRepository implements DocumentMetadataRepository {
    private final Map<String, List<VersionedDocument>> bySource = new LinkedHashMap<>();
    private final Map<String, VersionedDocument> byId = new LinkedHashMap<>();

    @Override
    public synchronized VersionedDocument saveIfChanged(DocumentRecord document) {
        List<VersionedDocument> versions = bySource.get(document.getSourcePath());
        if (versions == null) {
            versions = new ArrayList<>();
            bySource.put(document.getSourcePath(), versions);
        }
        if (!versions.isEmpty()) {
            VersionedDocument current = versions.get(versions.size() - 1);
            if (current.getDocument().getContentHash().equals(document.getContentHash())) {
                return new VersionedDocument(current.getDocument(), current.getVersion(), false);
            }
        }
        VersionedDocument saved = new VersionedDocument(document, versions.size() + 1, true);
        versions.add(saved);
        byId.put(document.getDocumentId(), saved);
        return saved;
    }

    @Override
    public synchronized Optional<VersionedDocument> findCurrent(String sourcePath) {
        List<VersionedDocument> versions = bySource.get(sourcePath);
        return versions == null || versions.isEmpty()
                ? Optional.<VersionedDocument>empty()
                : Optional.of(versions.get(versions.size() - 1));
    }

    @Override
    public synchronized Optional<VersionedDocument> findById(String documentId) {
        return Optional.ofNullable(byId.get(documentId));
    }

    @Override
    public synchronized List<VersionedDocument> findVersions(String sourcePath) {
        List<VersionedDocument> versions = bySource.get(sourcePath);
        return versions == null
                ? Collections.<VersionedDocument>emptyList()
                : Collections.unmodifiableList(new ArrayList<>(versions));
    }
}
