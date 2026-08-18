package com.nebula.search;

import com.nebula.ingestion.DocumentIngestor;

import java.util.List;

/** Tests deterministic HNSW insertion and exact-baseline recall on a small corpus. */
public final class HnswIndexTest {
    public static void main(String[] args) {
        HashingEmbeddingModel model = new HashingEmbeddingModel(128);
        VectorIndex exact = new VectorIndex(model.dimension());
        HnswIndex ann = new HnswIndex(model.dimension(), 8, 32, 42L);
        DocumentIngestor ingestor = new DocumentIngestor();
        for (int i = 0; i < 20; i++) {
            String path = "docs/doc-" + i + ".md";
            String text = "# Document " + i + "\n\nDistributed search topic " + (i % 4) + " with routing and retrieval.";
            com.nebula.ingestion.DocumentRecord document = ingestor.ingest(path, text);
            double[] vector = model.embed(text);
            exact.add(document, vector, model.modelId());
            ann.add(document, vector, model.modelId());
        }
        double[] query = model.embed("distributed search topic 2");
        List<VectorIndex.VectorMatch> exactTop = exact.search(query, 5);
        List<HnswIndex.HnswMatch> annTop = ann.search(query, 5, 32);
        int overlap = 0;
        for (VectorIndex.VectorMatch exactMatch : exactTop) {
            for (HnswIndex.HnswMatch annMatch : annTop) {
                if (exactMatch.getDocument().getDocument().getDocumentId().equals(annMatch.getDocument().getDocument().getDocumentId())) overlap++;
            }
        }
        check(ann.documentCount() == 20, "all vectors are inserted");
        check(overlap >= 4, "HNSW maintains high recall on the baseline corpus");
        check(ann.maxLevel() >= 0, "multi-layer entry point is created");
        System.out.println("HnswIndexTest: PASS");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
