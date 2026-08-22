package com.nebula.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Fuses lexical, semantic, freshness, source-authority, and graph signals. */
public final class TrustOrientedHybridSearchEngine {
    private final HybridSearchEngine hybrid;
    private final TrustMetadataStore metadataStore;
    private final FreshnessScorer freshnessScorer;
    private final PageRankResult pageRank;
    private final RankingConfiguration configuration;

    public TrustOrientedHybridSearchEngine(HybridSearchEngine hybrid, TrustMetadataStore metadataStore,
                                           FreshnessScorer freshnessScorer, PageRankResult pageRank,
                                           RankingConfiguration configuration) {
        if (hybrid == null || metadataStore == null || freshnessScorer == null || configuration == null) {
            throw new IllegalArgumentException("hybrid, metadata, freshness, and configuration are required");
        }
        this.hybrid = hybrid;
        this.metadataStore = metadataStore;
        this.freshnessScorer = freshnessScorer;
        this.pageRank = pageRank;
        this.configuration = configuration;
    }

    public RankingConfiguration getConfiguration() { return configuration; }

    public List<SearchResult> search(String query, int limit, long nowEpochMillis) {
        if (limit <= 0) throw new IllegalArgumentException("limit must be positive");
        List<SearchResult> candidates = hybrid.search(query, Math.max(limit * 5, limit));
        if (candidates.isEmpty()) return Collections.emptyList();
        double graphMin = Double.POSITIVE_INFINITY;
        double graphMax = Double.NEGATIVE_INFINITY;
        for (SearchResult candidate : candidates) {
            double value = pageRank == null ? 0.5 : pageRank.score(candidate.getDocument().getSourcePath());
            graphMin = Math.min(graphMin, value);
            graphMax = Math.max(graphMax, value);
        }
        List<SearchResult> ranked = new ArrayList<>();
        for (SearchResult candidate : candidates) {
            double lexical = value(candidate, "signal:lexical");
            double semantic = value(candidate, "signal:semantic");
            Optional<DocumentTrustMetadata> metadata = metadataStore.find(candidate.getDocument().getSourcePath());
            double authority = metadata.map(DocumentTrustMetadata::getAuthority).orElse(0.5);
            double freshness = metadata.isPresent()
                    ? freshnessScorer.score(metadata.get().getLastVerifiedEpochMillis(), nowEpochMillis) : 0.5;
            double graph = pageRank == null ? 0.5 : pageRank.score(candidate.getDocument().getSourcePath());
            graph = graphMax == graphMin ? 0.5 : (graph - graphMin) / (graphMax - graphMin);
            double score = configuration.getLexicalWeight() * lexical
                    + configuration.getSemanticWeight() * semantic
                    + configuration.getAuthorityWeight() * authority
                    + configuration.getFreshnessWeight() * freshness
                    + configuration.getGraphWeight() * graph;
            Map<String, Double> explanation = new LinkedHashMap<>(candidate.getTermContributions());
            explanation.put("signal:lexical", lexical);
            explanation.put("signal:semantic", semantic);
            explanation.put("signal:authority", authority);
            explanation.put("signal:freshness", freshness);
            explanation.put("signal:pagerank", graph);
            explanation.put("signal:trust_oriented_hybrid", score);
            ranked.add(new SearchResult(candidate.getDocument(), score, explanation));
        }
        ranked.sort(new Comparator<SearchResult>() {
            @Override
            public int compare(SearchResult left, SearchResult right) {
                int score = Double.compare(right.getScore(), left.getScore());
                return score != 0 ? score : left.getDocument().getDocumentId().compareTo(right.getDocument().getDocumentId());
            }
        });
        return Collections.unmodifiableList(new ArrayList<>(ranked.subList(0, Math.min(limit, ranked.size()))));
    }

    private static double value(SearchResult result, String key) {
        Double value = result.getTermContributions().get(key);
        return value == null ? 0.0 : value;
    }
}
