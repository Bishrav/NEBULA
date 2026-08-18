package com.nebula.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Explainable score fusion for lexical and exact semantic retrieval. */
public final class HybridSearchEngine {
    private static final double DEFAULT_LEXICAL_WEIGHT = 0.5;
    private static final double DEFAULT_SEMANTIC_WEIGHT = 0.5;
    private static final int CANDIDATE_MULTIPLIER = 5;

    private final BM25SearchEngine lexical;
    private final SemanticSearchEngine semantic;
    private final double lexicalWeight;
    private final double semanticWeight;

    public HybridSearchEngine(BM25SearchEngine lexical, SemanticSearchEngine semantic) {
        this(lexical, semantic, DEFAULT_LEXICAL_WEIGHT, DEFAULT_SEMANTIC_WEIGHT);
    }

    public HybridSearchEngine(BM25SearchEngine lexical, SemanticSearchEngine semantic,
                              double lexicalWeight, double semanticWeight) {
        if (lexical == null || semantic == null) throw new IllegalArgumentException("search engines are required");
        if (lexicalWeight < 0.0 || semanticWeight < 0.0 || lexicalWeight + semanticWeight <= 0.0) {
            throw new IllegalArgumentException("weights must be non-negative and have a positive sum");
        }
        this.lexical = lexical;
        this.semantic = semantic;
        double total = lexicalWeight + semanticWeight;
        this.lexicalWeight = lexicalWeight / total;
        this.semanticWeight = semanticWeight / total;
    }

    public List<SearchResult> search(String query, int limit) {
        if (limit <= 0) throw new IllegalArgumentException("limit must be positive");
        int candidateLimit = limit > Integer.MAX_VALUE / CANDIDATE_MULTIPLIER
                ? Integer.MAX_VALUE : limit * CANDIDATE_MULTIPLIER;
        List<SearchResult> lexicalResults = lexical.search(query, candidateLimit);
        List<SearchResult> semanticResults = semantic.search(query, candidateLimit);
        Map<String, Candidate> candidates = new LinkedHashMap<>();
        addCandidates(candidates, lexicalResults, true);
        addCandidates(candidates, semanticResults, false);

        double lexicalMin = minScore(candidates, true);
        double lexicalMax = maxScore(candidates, true);
        double semanticMin = minScore(candidates, false);
        double semanticMax = maxScore(candidates, false);
        List<SearchResult> fused = new ArrayList<>();
        for (Candidate candidate : candidates.values()) {
            double normalizedLexical = normalize(candidate.lexicalScore, candidate.hasLexical,
                    lexicalMin, lexicalMax);
            double normalizedSemantic = normalize(candidate.semanticScore, candidate.hasSemantic,
                    semanticMin, semanticMax);
            double score = lexicalWeight * normalizedLexical + semanticWeight * normalizedSemantic;
            Map<String, Double> explanation = new LinkedHashMap<>();
            explanation.put("signal:lexical", normalizedLexical);
            explanation.put("signal:semantic", normalizedSemantic);
            explanation.put("signal:hybrid", score);
            fused.add(new SearchResult(candidate.document, score, explanation));
        }
        Collections.sort(fused, new Comparator<SearchResult>() {
            @Override
            public int compare(SearchResult left, SearchResult right) {
                int scoreOrder = Double.compare(right.getScore(), left.getScore());
                return scoreOrder != 0 ? scoreOrder
                        : left.getDocument().getDocumentId().compareTo(right.getDocument().getDocumentId());
            }
        });
        return Collections.unmodifiableList(new ArrayList<>(fused.subList(0, Math.min(limit, fused.size()))));
    }

    private void addCandidates(Map<String, Candidate> candidates, List<SearchResult> results, boolean isLexical) {
        for (SearchResult result : results) {
            String id = result.getDocument().getDocumentId();
            Candidate candidate = candidates.get(id);
            if (candidate == null) {
                candidate = new Candidate(result.getDocument());
                candidates.put(id, candidate);
            }
            if (isLexical) {
                candidate.lexicalScore = result.getScore();
                candidate.hasLexical = true;
            } else {
                candidate.semanticScore = result.getScore();
                candidate.hasSemantic = true;
            }
        }
    }

    private double minScore(Map<String, Candidate> candidates, boolean lexicalChannel) {
        double min = Double.POSITIVE_INFINITY;
        for (Candidate candidate : candidates.values()) {
            if ((lexicalChannel ? candidate.hasLexical : candidate.hasSemantic)) {
                min = Math.min(min, lexicalChannel ? candidate.lexicalScore : candidate.semanticScore);
            }
        }
        return min;
    }

    private double maxScore(Map<String, Candidate> candidates, boolean lexicalChannel) {
        double max = Double.NEGATIVE_INFINITY;
        for (Candidate candidate : candidates.values()) {
            if ((lexicalChannel ? candidate.hasLexical : candidate.hasSemantic)) {
                max = Math.max(max, lexicalChannel ? candidate.lexicalScore : candidate.semanticScore);
            }
        }
        return max;
    }

    private double normalize(double score, boolean present, double min, double max) {
        if (!present) return 0.0;
        if (Double.compare(min, max) == 0) return 1.0;
        return (score - min) / (max - min);
    }

    private static final class Candidate {
        private final com.nebula.ingestion.DocumentRecord document;
        private double lexicalScore;
        private double semanticScore;
        private boolean hasLexical;
        private boolean hasSemantic;

        private Candidate(com.nebula.ingestion.DocumentRecord document) {
            this.document = document;
        }
    }
}
