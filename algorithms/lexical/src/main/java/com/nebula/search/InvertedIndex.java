package com.nebula.search;

import com.nebula.ingestion.DocumentRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** In-memory positional inverted index for the lexical-search baseline. */
public final class InvertedIndex {
    private final Map<String, List<Posting>> postingsByTerm = new LinkedHashMap<>();
    private final Map<String, IndexedDocument> documentsById = new LinkedHashMap<>();
    private long totalTokenCount;

    public synchronized boolean add(DocumentRecord document) {
        if (documentsById.containsKey(document.getDocumentId())) return false;

        List<String> terms = TextAnalyzer.analyze(document.getText());
        Map<String, List<Integer>> positionsByTerm = new LinkedHashMap<>();
        for (int position = 0; position < terms.size(); position++) {
            String term = terms.get(position);
            List<Integer> positions = positionsByTerm.get(term);
            if (positions == null) {
                positions = new ArrayList<>();
                positionsByTerm.put(term, positions);
            }
            positions.add(position);
        }

        for (Map.Entry<String, List<Integer>> entry : positionsByTerm.entrySet()) {
            List<Posting> postings = postingsByTerm.get(entry.getKey());
            if (postings == null) {
                postings = new ArrayList<>();
                postingsByTerm.put(entry.getKey(), postings);
            }
            postings.add(new Posting(document.getDocumentId(), entry.getValue()));
        }
        documentsById.put(document.getDocumentId(), new IndexedDocument(document, terms.size()));
        totalTokenCount += terms.size();
        return true;
    }

    /** Adds a decoded immutable segment without re-tokenizing its documents. */
    public synchronized int addSegment(PersistedIndexSegment segment) {
        if (segment == null) throw new IllegalArgumentException("segment must not be null");
        int addedDocuments = 0;
        for (IndexedDocument indexed : segment.documents()) {
            String documentId = indexed.getDocument().getDocumentId();
            if (documentsById.containsKey(documentId)) continue;
            documentsById.put(documentId, indexed);
            totalTokenCount += indexed.getDocumentLength();
            addedDocuments++;
        }
        for (String term : segment.terms()) {
            List<Posting> target = postingsByTerm.get(term);
            if (target == null) {
                target = new ArrayList<>();
                postingsByTerm.put(term, target);
            }
            for (Posting posting : segment.postings(term)) {
                if (documentsById.containsKey(posting.getDocumentId()) && !containsDocument(target, posting.getDocumentId())) {
                    target.add(posting);
                }
            }
        }
        return addedDocuments;
    }

    private static boolean containsDocument(List<Posting> postings, String documentId) {
        for (Posting posting : postings) {
            if (posting.getDocumentId().equals(documentId)) return true;
        }
        return false;
    }

    public synchronized List<Posting> postings(String term) {
        List<Posting> postings = postingsByTerm.get(term.toLowerCase());
        return postings == null
                ? Collections.<Posting>emptyList()
                : Collections.unmodifiableList(new ArrayList<>(postings));
    }

    public synchronized IndexedDocument document(String documentId) {
        return documentsById.get(documentId);
    }

    public synchronized int documentCount() { return documentsById.size(); }

    public synchronized int documentFrequency(String term) { return postings(term).size(); }

    public synchronized double averageDocumentLength() {
        return documentsById.isEmpty() ? 0.0 : (double) totalTokenCount / documentsById.size();
    }

    public synchronized List<IndexedDocument> documents() {
        return Collections.unmodifiableList(new ArrayList<>(documentsById.values()));
    }

    public synchronized List<String> terms() {
        return Collections.unmodifiableList(new ArrayList<>(postingsByTerm.keySet()));
    }
}
