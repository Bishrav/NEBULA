package com.nebula.evaluation;

import com.nebula.ingestion.DocumentRecord;
import com.nebula.search.HnswIndex;
import com.nebula.search.VectorDocument;
import com.nebula.search.VectorIndex;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Separate ANN systems benchmark. Synthetic vectors measure scaling mechanics,
 * not semantic retrieval quality.
 */
public final class AnnScaleBenchmark {
    private AnnScaleBenchmark() { }

    public static void main(String[] args) throws Exception {
        if (args.length < 1 || args.length > 10) {
            System.err.println("Usage: AnnScaleBenchmark <output.json> [sizes] [dimension] [queries] [cutoff] [M] [efConstruction] [efSearch] [repeats] [seed]");
            System.exit(2);
        }
        Path output = Paths.get(args[0]);
        List<Integer> sizes = parseSizes(args.length > 1 ? args[1] : "10000,100000");
        int dimension = args.length > 2 ? Integer.parseInt(args[2]) : 128;
        int queries = args.length > 3 ? Integer.parseInt(args[3]) : 50;
        int cutoff = args.length > 4 ? Integer.parseInt(args[4]) : 10;
        int m = args.length > 5 ? Integer.parseInt(args[5]) : 16;
        int efConstruction = args.length > 6 ? Integer.parseInt(args[6]) : 200;
        int efSearch = args.length > 7 ? Integer.parseInt(args[7]) : 64;
        int repeats = args.length > 8 ? Integer.parseInt(args[8]) : 1;
        long seed = args.length > 9 ? Long.parseLong(args[9]) : 20260822L;
        if (dimension <= 0 || queries <= 0 || cutoff <= 0 || m <= 0 || efConstruction <= 0 || efSearch <= 0 || repeats <= 0) throw new IllegalArgumentException("benchmark parameters must be positive");

        StringBuilder json = new StringBuilder("{\"schemaVersion\":\"ann-scale-v1\",\"syntheticVectors\":true,\"dimension\":")
                .append(dimension).append(",\"queries\":").append(queries)
                .append(",\"cutoff\":").append(cutoff).append(",\"M\":").append(m)
                .append(",\"efConstruction\":").append(efConstruction).append(",\"efSearch\":").append(efSearch)
                .append(",\"repeats\":").append(repeats).append(",\"seed\":").append(seed).append(",\"results\":[");
        for (int index = 0; index < sizes.size(); index++) {
            if (index > 0) json.append(',');
            json.append(run(sizes.get(index), dimension, queries, cutoff, m, efConstruction, efSearch, repeats, seed));
        }
        json.append("]}\n");
        if (output.getParent() != null) Files.createDirectories(output.getParent());
        Files.write(output, json.toString().getBytes(StandardCharsets.UTF_8));
        System.out.println("GENERATED: " + output.toAbsolutePath());
    }

    private static String run(int size, int dimension, int queryCount, int cutoff, int m, int efConstruction, int efSearch, int repeats, long seed) {
        if (size < cutoff) throw new IllegalArgumentException("size must be at least cutoff");
        StringBuilder trials = new StringBuilder();
        for (int repeat = 0; repeat < repeats; repeat++) {
            if (repeat > 0) trials.append(',');
            Random random = new Random(seed + size + repeat);
            VectorIndex exact = new VectorIndex(dimension);
            HnswIndex ann = new HnswIndex(dimension, m, efConstruction, seed + size + repeat);
            long before = usedMemory();
            long buildStart = System.nanoTime();
            for (int index = 0; index < size; index++) {
                double[] vector = vector(random, dimension);
                DocumentRecord document = new DocumentRecord("v-" + index, "synthetic/" + index,
                        "synthetic-vector", "vector-" + index, "synthetic vector", "synthetic-" + index);
                exact.add(document, vector, "synthetic-v1-d" + dimension);
                ann.add(document, vector, "synthetic-v1-d" + dimension);
            }
            long buildMillis = (System.nanoTime() - buildStart) / 1_000_000L;
            long after = usedMemory();
            Random queryRandom = new Random(seed ^ size);
            List<Long> exactLatencies = new ArrayList<>();
            List<Long> annLatencies = new ArrayList<>();
            double recall = 0.0, recallAt1 = 0.0, recallAt5 = 0.0, recallAt10 = 0.0;
            int evaluationCutoff = Math.max(cutoff, 10);
            for (int query = 0; query < queryCount; query++) {
                double[] vector = vector(queryRandom, dimension);
            long start = System.nanoTime();
            List<VectorIndex.VectorMatch> exactResults = exact.search(vector, evaluationCutoff);
            exactLatencies.add(System.nanoTime() - start);
            start = System.nanoTime();
            List<HnswIndex.HnswMatch> annResults = ann.search(vector, evaluationCutoff, efSearch);
            annLatencies.add(System.nanoTime() - start);
            Set<String> expected = new HashSet<>();
            for (VectorIndex.VectorMatch result : exactResults) expected.add(result.getDocument().getDocument().getDocumentId());
            int overlap = 0, overlap1 = 0, overlap5 = 0, overlap10 = 0;
            for (HnswIndex.HnswMatch result : annResults) {
                if (expected.contains(result.getDocument().getDocument().getDocumentId())) overlap++;
            }
            recall += expected.isEmpty() ? 1.0 : (double) overlap / expected.size();
            for (int i = 0; i < Math.min(annResults.size(), evaluationCutoff); i++) {
                String id = annResults.get(i).getDocument().getDocument().getDocumentId();
                if (i < 1 && exactResults.stream().limit(1).anyMatch(value -> value.getDocument().getDocument().getDocumentId().equals(id))) overlap1++;
                if (i < 5 && exactResults.stream().limit(5).anyMatch(value -> value.getDocument().getDocument().getDocumentId().equals(id))) overlap5++;
                if (i < 10 && exactResults.stream().limit(10).anyMatch(value -> value.getDocument().getDocument().getDocumentId().equals(id))) overlap10++;
            }
            recallAt1 += overlap1; recallAt5 += overlap5 / 5.0; recallAt10 += overlap10 / 10.0;
            }
            trials.append("{\"repeat\":").append(repeat).append(",\"buildMillis\":").append(buildMillis)
                    .append(",\"heapDeltaBytes\":").append(Math.max(0L, after - before)).append(",\"estimatedIndexBytes\":").append(ann.estimatedIndexBytes())
                    .append(",\"recallAtK\":").append(number(recall / queryCount)).append(",\"recallAt1\":").append(number(recallAt1 / queryCount))
                    .append(",\"recallAt5\":").append(number(recallAt5 / queryCount)).append(",\"recallAt10\":").append(number(recallAt10 / queryCount))
                    .append(",\"exactP50Millis\":").append(number(percentile(exactLatencies, 0.50) / 1_000_000.0))
                    .append(",\"exactP95Millis\":").append(number(percentile(exactLatencies, 0.95) / 1_000_000.0)).append(",\"exactP99Millis\":").append(number(percentile(exactLatencies, 0.99) / 1_000_000.0))
                    .append(",\"hnswP50Millis\":").append(number(percentile(annLatencies, 0.50) / 1_000_000.0))
                    .append(",\"hnswP95Millis\":").append(number(percentile(annLatencies, 0.95) / 1_000_000.0)).append(",\"hnswP99Millis\":").append(number(percentile(annLatencies, 0.99) / 1_000_000.0)).append('}');
        }
        return "{\"vectors\":" + size + ",\"M\":" + m + ",\"efConstruction\":" + efConstruction + ",\"efSearch\":" + efSearch + ",\"trials\":[" + trials + "]}";
    }

    private static double[] vector(Random random, int dimension) {
        double[] vector = new double[dimension];
        double magnitude = 0.0;
        for (int index = 0; index < dimension; index++) {
            vector[index] = random.nextGaussian();
            magnitude += vector[index] * vector[index];
        }
        magnitude = Math.sqrt(magnitude);
        for (int index = 0; index < dimension; index++) vector[index] /= magnitude;
        return vector;
    }

    private static List<Integer> parseSizes(String value) {
        List<Integer> sizes = new ArrayList<>();
        Set<Integer> unique = new HashSet<>();
        for (String part : value.split(",")) {
            int size = Integer.parseInt(part.trim());
            if (size <= 0 || !unique.add(size)) throw new IllegalArgumentException("sizes must be positive and unique");
            sizes.add(size);
        }
        if (sizes.isEmpty()) throw new IllegalArgumentException("at least one size is required");
        return sizes;
    }

    private static long percentile(List<Long> values, double fraction) {
        List<Long> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int index = Math.min(sorted.size() - 1, (int) Math.ceil((sorted.size() - 1) * fraction));
        return sorted.get(index);
    }

    private static long usedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    private static String number(double value) { return String.format(java.util.Locale.ROOT, "%.6f", value); }
}
