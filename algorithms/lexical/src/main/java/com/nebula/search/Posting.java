package com.nebula.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A term's occurrence information for one document. Positions are zero-based. */
public final class Posting {
    private final String documentId;
    private final List<Integer> positions;

    public Posting(String documentId, List<Integer> positions) {
        this.documentId = documentId;
        this.positions = Collections.unmodifiableList(new ArrayList<>(positions));
    }

    public String getDocumentId() { return documentId; }
    public List<Integer> getPositions() { return positions; }
    public int getTermFrequency() { return positions.size(); }
}
