package com.nebula.search;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** In-memory trust metadata registry; a persistent adapter will follow the metadata service. */
public final class TrustMetadataStore {
    private final Map<String, DocumentTrustMetadata> bySourcePath = new LinkedHashMap<>();

    public synchronized void register(DocumentTrustMetadata metadata) {
        bySourcePath.put(metadata.getSourcePath(), metadata);
    }

    public synchronized Optional<DocumentTrustMetadata> find(String sourcePath) {
        return Optional.ofNullable(bySourcePath.get(sourcePath));
    }
}
