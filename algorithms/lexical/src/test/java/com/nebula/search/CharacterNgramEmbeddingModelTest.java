package com.nebula.search;

import com.nebula.ingestion.DocumentIngestor;

import java.util.List;

/** Tests deterministic subword embeddings and catalog model injection. */
public final class CharacterNgramEmbeddingModelTest {
    public static void main(String[] args) {
        CharacterNgramEmbeddingModel model = new CharacterNgramEmbeddingModel(128);
        double[] first = model.embed("Deployment gateway");
        double[] second = model.embed("Deployment gateway");
        check(first.length == 128, "embedding dimension is stable");
        check(model.modelId().contains("char-ngram"), "model identity is explicit");
        check(VectorIndex.cosineSimilarity(first, second) > 0.99, "embedding is deterministic");
        check(VectorIndex.cosineSimilarity(first, model.embed("deployment")) > 0.0,
                "related text shares subword evidence");

        SearchCatalog catalog = new SearchCatalog(new DocumentIngestor(), new InvertedIndex(), model);
        catalog.indexMarkdown("docs/deployment.md", "# Deployment\n\nDeploy services through a gateway.");
        List<SearchResult> results = catalog.semanticSearch("deployment gateway", 1);
        check(results.size() == 1, "custom embedding model is used by catalog");
        check(results.get(0).getTermContributions().get("embedding:dimension") == 128.0,
                "semantic explanation reports custom model dimension");
        System.out.println("CharacterNgramEmbeddingModelTest: PASS");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
