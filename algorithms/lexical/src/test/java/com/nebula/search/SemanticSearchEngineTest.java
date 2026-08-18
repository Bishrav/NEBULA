package com.nebula.search;

import com.nebula.ingestion.DocumentIngestor;

import java.util.List;

/** Tests deterministic embeddings, cosine similarity, and exact semantic ranking. */
public final class SemanticSearchEngineTest {
    public static void main(String[] args) {
        HashingEmbeddingModel model = new HashingEmbeddingModel(128);
        VectorIndex index = new VectorIndex(model.dimension());
        DocumentIngestor ingestor = new DocumentIngestor();
        index.add(ingestor.ingest("docs/routing.md", "# Routing\n\nRoute requests through a gateway."), model.embed("Route requests through a gateway."), model.modelId());
        index.add(ingestor.ingest("docs/storage.md", "# Storage\n\nPersist objects in a database."), model.embed("Persist objects in a database."), model.modelId());

        List<SearchResult> results = new SemanticSearchEngine(model, index).search("gateway routes requests", 2);
        check(results.size() == 2, "exact vector search returns top-k");
        check(results.get(0).getDocument().getSourcePath().equals("docs/routing.md"), "semantic match ranks first");
        check(results.get(0).getTermContributions().containsKey("signal:semantic"), "semantic score is explained");
        check(model.embed("same text").length == 128, "embedding dimension is stable");
        System.out.println("SemanticSearchEngineTest: PASS");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
