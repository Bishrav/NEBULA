package com.nebula.evaluation;

import com.nebula.ingestion.DocumentRecord;
import com.nebula.search.EmbeddingModel;
import com.nebula.search.HashingEmbeddingModel;
import com.nebula.search.HnswIndex;
import com.nebula.search.VectorIndex;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Measures custom HNSW recall and latency against exact cosine retrieval. */
public final class AnnBenchmark {
    public Result evaluate(List<DocumentRecord> documents, List<EvaluationQuery> queries, int cutoff) {
        EmbeddingModel model = new HashingEmbeddingModel(128);
        VectorIndex exact = new VectorIndex(model.dimension());
        HnswIndex ann = new HnswIndex(model.dimension(), 8, 64, 42L);
        for (DocumentRecord document : documents) {
            double[] vector = model.embed(document.getText());
            exact.add(document, vector, model.modelId());
            ann.add(document, vector, model.modelId());
        }
        double recall = 0.0;
        long exactNanos = 0L;
        long annNanos = 0L;
        int evaluated = 0;
        for (EvaluationQuery query : queries) {
            double[] vector = model.embed(query.getText());
            long start = System.nanoTime();
            List<VectorIndex.VectorMatch> exactMatches = exact.search(vector, cutoff);
            exactNanos += System.nanoTime() - start;
            start = System.nanoTime();
            List<HnswIndex.HnswMatch> annMatches = ann.search(vector, cutoff, 32);
            annNanos += System.nanoTime() - start;
            Set<String> exactIds = new HashSet<>();
            for (VectorIndex.VectorMatch match : exactMatches) exactIds.add(match.getDocument().getDocument().getDocumentId());
            int overlap = 0;
            for (HnswIndex.HnswMatch match : annMatches) {
                if (exactIds.contains(match.getDocument().getDocument().getDocumentId())) overlap++;
            }
            recall += exactIds.isEmpty() ? 1.0 : (double) overlap / exactIds.size();
            evaluated++;
        }
        return new Result(documents.size(), evaluated, cutoff, evaluated == 0 ? 0.0 : recall / evaluated,
                millis(exactNanos, evaluated), millis(annNanos, evaluated), ann.maxLevel());
    }

    private static double millis(long nanos, int count) {
        return count == 0 ? 0.0 : nanos / 1_000_000.0 / count;
    }

    public static final class Result {
        private final int documents;
        private final int queries;
        private final int cutoff;
        private final double recall;
        private final double exactLatencyMs;
        private final double annLatencyMs;
        private final int maxLevel;

        private Result(int documents, int queries, int cutoff, double recall,
                       double exactLatencyMs, double annLatencyMs, int maxLevel) {
            this.documents = documents; this.queries = queries; this.cutoff = cutoff;
            this.recall = recall; this.exactLatencyMs = exactLatencyMs;
            this.annLatencyMs = annLatencyMs; this.maxLevel = maxLevel;
        }
        public int getDocuments() { return documents; }
        public int getQueries() { return queries; }
        public int getCutoff() { return cutoff; }
        public double getRecall() { return recall; }
        public double getExactLatencyMs() { return exactLatencyMs; }
        public double getAnnLatencyMs() { return annLatencyMs; }
        public int getMaxLevel() { return maxLevel; }
    }
}
