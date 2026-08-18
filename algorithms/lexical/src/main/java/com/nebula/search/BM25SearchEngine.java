package com.nebula.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** First-principles BM25 query engine over an InvertedIndex. */
public final class BM25SearchEngine {
    private static final double DEFAULT_K1 = 1.2;
    private static final double DEFAULT_B = 0.75;

    private final InvertedIndex index;
    private final double k1;
    private final double b;

    public BM25SearchEngine(InvertedIndex index) {
        this(index, DEFAULT_K1, DEFAULT_B);
    }

    public BM25SearchEngine(InvertedIndex index, double k1, double b) {
        if (index == null) throw new IllegalArgumentException("index must not be null");
        if (k1 < 0.0) throw new IllegalArgumentException("k1 must not be negative");
        if (b < 0.0 || b > 1.0) throw new IllegalArgumentException("b must be between zero and one");
        this.index = index;
        this.k1 = k1;
        this.b = b;
    }

    public List<SearchResult> search(String query, int limit) {
        if (limit <= 0) throw new IllegalArgumentException("limit must be positive");
        SearchQuery parsedQuery = SearchQuery.parse(query);
        if (parsedQuery.getTerms().isEmpty() || index.documentCount() == 0) return Collections.emptyList();

        Set<String> queryTerms = new LinkedHashSet<>(parsedQuery.getTerms());
        Map<String, Map<String, Double>> contributionsByDocument = new LinkedHashMap<>();
        for (String term : queryTerms) {
            List<Posting> postings = index.postings(term);
            if (postings.isEmpty()) continue;
            double idf = inverseDocumentFrequency(index.documentCount(), postings.size());
            for (Posting posting : postings) {
                IndexedDocument indexed = index.document(posting.getDocumentId());
                double contribution = idf * termFrequencyComponent(
                        posting.getTermFrequency(), indexed.getDocumentLength(), index.averageDocumentLength());
                Map<String, Double> contributions = contributionsByDocument.get(posting.getDocumentId());
                if (contributions == null) {
                    contributions = new LinkedHashMap<>();
                    contributionsByDocument.put(posting.getDocumentId(), contributions);
                }
                contributions.put(term, contribution);
            }
        }

        List<SearchResult> results = new ArrayList<>();
        for (Map.Entry<String, Map<String, Double>> entry : contributionsByDocument.entrySet()) {
            if (!matchesAllPhrases(entry.getKey(), parsedQuery.getPhrases())) continue;
            double score = 0.0;
            for (double contribution : entry.getValue().values()) score += contribution;
            results.add(new SearchResult(index.document(entry.getKey()).getDocument(), score, entry.getValue()));
        }
        Collections.sort(results, new Comparator<SearchResult>() {
            @Override
            public int compare(SearchResult left, SearchResult right) {
                int scoreOrder = Double.compare(right.getScore(), left.getScore());
                return scoreOrder != 0
                        ? scoreOrder
                        : left.getDocument().getDocumentId().compareTo(right.getDocument().getDocumentId());
            }
        });
        return Collections.unmodifiableList(new ArrayList<>(results.subList(0, Math.min(limit, results.size()))));
    }

    private boolean matchesAllPhrases(String documentId, List<List<String>> phrases) {
        for (List<String> phrase : phrases) {
            if (!PhraseMatcher.matches(index, documentId, phrase)) return false;
        }
        return true;
    }

    static double inverseDocumentFrequency(int documentCount, int documentFrequency) {
        return Math.log(1.0 + (documentCount - documentFrequency + 0.5) / (documentFrequency + 0.5));
    }

    private double termFrequencyComponent(int termFrequency, int documentLength, double averageLength) {
        if (averageLength == 0.0) return 0.0;
        double normalization = 1.0 - b + b * documentLength / averageLength;
        return (termFrequency * (k1 + 1.0)) / (termFrequency + k1 * normalization);
    }
}
