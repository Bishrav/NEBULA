package com.nebula.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Read-only representation of an immutable on-disk index segment. */
public final class PersistedIndexSegment {
    private final Map<String, IndexedDocument> documentsById;
    private final Map<String, List<Posting>> postingsByTerm;

    PersistedIndexSegment(Map<String, IndexedDocument> documentsById,
                          Map<String, List<Posting>> postingsByTerm) {
        this.documentsById = Collections.unmodifiableMap(new LinkedHashMap<>(documentsById));
        Map<String, List<Posting>> copiedPostings = new LinkedHashMap<>();
        for (Map.Entry<String, List<Posting>> entry : postingsByTerm.entrySet()) {
            copiedPostings.put(entry.getKey(), Collections.unmodifiableList(new ArrayList<>(entry.getValue())));
        }
        this.postingsByTerm = Collections.unmodifiableMap(copiedPostings);
    }

    public int documentCount() { return documentsById.size(); }

    public List<IndexedDocument> documents() {
        return Collections.unmodifiableList(new ArrayList<>(documentsById.values()));
    }

    public IndexedDocument document(String documentId) { return documentsById.get(documentId); }

    public List<Posting> postings(String term) {
        List<Posting> postings = postingsByTerm.get(term);
        return postings == null ? Collections.<Posting>emptyList() : postings;
    }

    public List<String> terms() {
        return Collections.unmodifiableList(new ArrayList<>(postingsByTerm.keySet()));
    }
}
