package com.nebula.search;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Exact positional phrase matching over the inverted index. */
public final class PhraseMatcher {
    private PhraseMatcher() { }

    public static boolean matches(InvertedIndex index, String documentId, List<String> phraseTerms) {
        if (phraseTerms == null || phraseTerms.isEmpty()) return true;
        List<Posting> firstPostings = index.postings(phraseTerms.get(0));
        Set<Integer> starts = new HashSet<>();
        for (Posting posting : firstPostings) {
            if (posting.getDocumentId().equals(documentId)) starts.addAll(posting.getPositions());
        }
        if (starts.isEmpty()) return false;

        for (int termIndex = 1; termIndex < phraseTerms.size(); termIndex++) {
            Set<Integer> positions = positionsFor(index, documentId, phraseTerms.get(termIndex));
            Set<Integer> matchingStarts = new HashSet<>();
            for (Integer start : starts) {
                if (positions.contains(start + termIndex)) matchingStarts.add(start);
            }
            starts = matchingStarts;
            if (starts.isEmpty()) return false;
        }
        return true;
    }

    private static Set<Integer> positionsFor(InvertedIndex index, String documentId, String term) {
        Set<Integer> positions = new HashSet<>();
        for (Posting posting : index.postings(term)) {
            if (posting.getDocumentId().equals(documentId)) positions.addAll(posting.getPositions());
        }
        return positions;
    }
}
