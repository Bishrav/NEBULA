package com.nebula.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Reranks lexical candidates with freshness and source-authority signals. */
public final class TrustAwareSearchEngine {
    private static final double DEFAULT_LEXICAL_WEIGHT = 0.70;
    private static final double DEFAULT_AUTHORITY_WEIGHT = 0.20;
    private static final double DEFAULT_FRESHNESS_WEIGHT = 0.10;

    private final BM25SearchEngine lexicalSearch;
    private final TrustMetadataStore metadataStore;
    private final FreshnessScorer freshnessScorer;
    private final PageRankResult pageRank;
    private final double lexicalWeight;
    private final double authorityWeight;
    private final double freshnessWeight;

    public TrustAwareSearchEngine(BM25SearchEngine lexicalSearch, TrustMetadataStore metadataStore) {
        this(lexicalSearch, metadataStore, new FreshnessScorer(30.0),
                DEFAULT_LEXICAL_WEIGHT, DEFAULT_AUTHORITY_WEIGHT, DEFAULT_FRESHNESS_WEIGHT);
    }

    public TrustAwareSearchEngine(BM25SearchEngine lexicalSearch, TrustMetadataStore metadataStore,
                                  FreshnessScorer freshnessScorer, double lexicalWeight,
                                  double authorityWeight, double freshnessWeight) {
        this(lexicalSearch, metadataStore, freshnessScorer, lexicalWeight, authorityWeight, freshnessWeight, null);
    }

    public TrustAwareSearchEngine(BM25SearchEngine lexicalSearch, TrustMetadataStore metadataStore,
                                  FreshnessScorer freshnessScorer, double lexicalWeight,
                                  double authorityWeight, double freshnessWeight, PageRankResult pageRank) {
        if (lexicalSearch == null || metadataStore == null || freshnessScorer == null) {
            throw new IllegalArgumentException("search, metadata, and freshness scorer are required");
        }
        if (lexicalWeight < 0 || authorityWeight < 0 || freshnessWeight < 0
                || lexicalWeight + authorityWeight + freshnessWeight <= 0) {
            throw new IllegalArgumentException("trust weights must be non-negative and non-zero");
        }
        this.lexicalSearch = lexicalSearch;
        this.metadataStore = metadataStore;
        this.freshnessScorer = freshnessScorer;
        this.pageRank = pageRank;
        double total = lexicalWeight + authorityWeight + freshnessWeight;
        this.lexicalWeight = lexicalWeight / total;
        this.authorityWeight = authorityWeight / total;
        this.freshnessWeight = freshnessWeight / total;
    }

    public List<SearchResult> search(String query, int limit, long nowEpochMillis) {
        if (limit <= 0) throw new IllegalArgumentException("limit must be positive");
        List<SearchResult> candidates = lexicalSearch.search(query, Math.max(limit * 5, limit));
        if (candidates.isEmpty()) return Collections.emptyList();
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (SearchResult candidate : candidates) {
            double graphScore = pageRank == null ? 0.5 : pageRank.score(candidate.getDocument().getSourcePath());
            min = Math.min(min, graphScore);
            max = Math.max(max, graphScore);
        }
        double graphMin = min;
        double graphMax = max;
        min = Double.POSITIVE_INFINITY;
        max = Double.NEGATIVE_INFINITY;
        for (SearchResult candidate : candidates) {
            min = Math.min(min, candidate.getScore());
            max = Math.max(max, candidate.getScore());
        }

        List<SearchResult> reranked = new ArrayList<>();
        for (SearchResult candidate : candidates) {
            double normalizedLexical = max == min ? 1.0 : (candidate.getScore() - min) / (max - min);
            Optional<DocumentTrustMetadata> metadata = metadataStore.find(candidate.getDocument().getSourcePath());
            double authority = metadata.isPresent() ? metadata.get().getAuthority() : 0.5;
            double graphAuthority = pageRank == null ? 0.5 : pageRank.score(candidate.getDocument().getSourcePath());
            if (graphMax != graphMin) graphAuthority = (graphAuthority - graphMin) / (graphMax - graphMin);
            else graphAuthority = 0.5;
            authority = 0.7 * authority + 0.3 * graphAuthority;
            double freshness = metadata.isPresent()
                    ? freshnessScorer.score(metadata.get().getLastVerifiedEpochMillis(), nowEpochMillis) : 0.5;
            double score = lexicalWeight * normalizedLexical
                    + authorityWeight * authority + freshnessWeight * freshness;
            Map<String, Double> contributions = new LinkedHashMap<>(candidate.getTermContributions());
            contributions.put("signal:lexical", normalizedLexical);
            contributions.put("signal:authority", authority);
            contributions.put("signal:pagerank", graphAuthority);
            contributions.put("signal:freshness", freshness);
            reranked.add(new SearchResult(candidate.getDocument(), score, contributions));
        }
        Collections.sort(reranked, new Comparator<SearchResult>() {
            @Override
            public int compare(SearchResult left, SearchResult right) {
                int scoreOrder = Double.compare(right.getScore(), left.getScore());
                return scoreOrder != 0 ? scoreOrder
                        : left.getDocument().getDocumentId().compareTo(right.getDocument().getDocumentId());
            }
        });
        return Collections.unmodifiableList(new ArrayList<>(reranked.subList(0, Math.min(limit, reranked.size()))));
    }
}
