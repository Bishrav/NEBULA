package com.nebula.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Semantic search adapter backed by the custom HNSW index. */
public final class HnswSemanticSearchEngine {
    private final EmbeddingModel model;
    private final HnswIndex index;
    private final int efSearch;

    public HnswSemanticSearchEngine(EmbeddingModel model, HnswIndex index, int efSearch) {
        if (model == null || index == null || model.dimension() != index.dimension()) {
            throw new IllegalArgumentException("model and index dimensions must match");
        }
        this.model = model;
        this.index = index;
        this.efSearch = efSearch;
    }

    public List<SearchResult> search(String query, int limit) {
        List<HnswIndex.HnswMatch> matches = index.search(model.embed(query), limit, efSearch);
        List<SearchResult> results = new ArrayList<>();
        for (HnswIndex.HnswMatch match : matches) {
            Map<String, Double> explanation = new LinkedHashMap<>();
            explanation.put("signal:semantic", match.getScore());
            explanation.put("ann:efSearch", (double) efSearch);
            results.add(new SearchResult(match.getDocument().getDocument(), match.getScore(), explanation));
        }
        return Collections.unmodifiableList(results);
    }
}
