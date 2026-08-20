package com.nebula.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Semantic retrieval over exact brute-force cosine similarity. */
public final class SemanticSearchEngine {
    private final EmbeddingModel model;
    private final VectorIndex index;

    public SemanticSearchEngine(EmbeddingModel model, VectorIndex index) {
        if (model == null || index == null || model.dimension() != index.dimension()) {
            throw new IllegalArgumentException("model and index dimensions must match");
        }
        this.model = model;
        this.index = index;
    }

    public List<SearchResult> search(String query, int limit) {
        List<VectorIndex.VectorMatch> matches = index.search(model.embed(query), limit);
        List<SearchResult> results = new ArrayList<>();
        for (VectorIndex.VectorMatch match : matches) {
            Map<String, Double> explanation = new LinkedHashMap<>();
            explanation.put("signal:semantic", match.getScore());
            explanation.put("embedding:dimension", (double) model.dimension());
            results.add(new SearchResult(match.getDocument().getDocument(), match.getScore(), explanation));
        }
        return Collections.unmodifiableList(results);
    }
}
