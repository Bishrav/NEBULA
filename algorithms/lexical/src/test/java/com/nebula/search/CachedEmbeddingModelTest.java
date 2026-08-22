package com.nebula.search;

import java.nio.file.Files;
import java.nio.file.Path;

public final class CachedEmbeddingModelTest {
    public static void main(String[] args) throws Exception {
        Path cache = Files.createTempFile("nebula-embedding", ".tsv");
        String text = "How should I configure replication?";
        Files.writeString(cache, "# schemaVersion=modern-embedding-cache-v1\n# modelId=BAAI/bge-base-en-v1.5\n# revision=a5beb1e3e68b9ab74eb54cfd186867f64f240e1a\n# license=MIT\n# dimension=3\n# normalized=true\n" + CachedEmbeddingModel.hash(text) + "\t0.0,0.6,0.8\n");
        CachedEmbeddingModel model = new CachedEmbeddingModel(cache);
        check(model.dimension() == 3, "dimension recorded");
        check(model.cachedVectors() == 1, "cache count");
        check(model.embed(text)[2] == 0.8, "cached vector loaded");
        check(model.modelId().contains("bge-base-en-v1.5"), "model id recorded");
        boolean missing = false;
        try { model.embed("missing"); } catch (IllegalArgumentException expected) { missing = true; }
        check(missing, "missing text fails explicitly");
        Files.deleteIfExists(cache);
        System.out.println("CachedEmbeddingModelTest: PASS");
    }

    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
