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
        if (args.length < 1 || args.length > 7) {
            System.err.println("Usage: AnnScaleBenchmark <output.json> [sizes] [dimension] [queries] [cutoff] [efSearch] [seed]");
            System.exit(2);
        }
        Path output = Paths.get(args[0]);
        List<Integer> sizes = parseSizes(args.length > 1 ? args[1] : "10000,100000");
        int dimension = args.length > 2 ? Integer.parseInt(args[2]) : 128;
        int queries = args.length > 3 ? Integer.parseInt(args[3]) : 50;
        int cutoff = args.length > 4 ? Integer.parseInt(args[4]) : 10;
        int efSearch = args.length > 5 ? Integer.parseInt(args[5]) : 64;
        long seed = args.length > 6 ? Long.parseLong(args[6]) : 20260822L;
        if (dimension <= 0 || queries <= 0 || cutoff <= 0 || efSearch <= 0) throw new IllegalArgumentException("benchmark parameters must be positive");

        StringBuilder json = new StringBuilder("{\"schemaVersion\":\"ann-scale-v1\",\"syntheticVectors\":true,\"dimension\":")
                .append(dimension).append(",\"queries\":").append(queries)
                .append(",\"cutoff\":").append(cutoff).append(",\"efSearch\":").append(efSearch)
                .append(",\"seed\":").append(seed).append(",\"results\":[");
        for (int index = 0; index < sizes.size(); index++) {
            if (index > 0) json.append(',');
            json.append(run(sizes.get(index), dimension, queries, cutoff, efSearch, seed));
        }
        json.append("]}\n");
        if (output.getParent() != null) Files.createDirectories(output.getParent());
        Files.write(output, json.toString().getBytes(StandardCharsets.UTF_8));
        System.out.println("GENERATED: " + output.toAbsolutePath());
    }

    private static String run(int size, int dimension, int queryCount, int cutoff, int efSearch, long seed) {
        if (size < cutoff) throw new IllegalArgumentException("size must be at least cutoff");
        Random random = new Random(seed + size);
        VectorIndex exact = new VectorIndex(dimension);
        HnswIndex ann = new HnswIndex(dimension, 16, 200, seed + size);
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
        double recall = 0.0;
        for (int query = 0; query < queryCount; query++) {
            double[] vector = vector(queryRandom, dimension);
            long start = System.nanoTime();
            List<VectorIndex.VectorMatch> exactResults = exact.search(vector, cutoff);
            exactLatencies.add(System.nanoTime() - start);
            start = System.nanoTime();
            List<HnswIndex.HnswMatch> annResults = ann.search(vector, cutoff, efSearch);
            annLatencies.add(System.nanoTime() - start);
            Set<String> expected = new HashSet<>();
            for (VectorIndex.VectorMatch result : exactResults) expected.add(result.getDocument().getDocument().getDocumentId());
            int overlap = 0;
            for (HnswIndex.HnswMatch result : annResults) {
                if (expected.contains(result.getDocument().getDocument().getDocumentId())) overlap++;
            }
            recall += expected.isEmpty() ? 1.0 : (double) overlap / expected.size();
        }
        return "{\"vectors\":" + size + ",\"buildMillis\":" + buildMillis
                + ",\"heapDeltaBytes\":" + Math.max(0L, after - before)
                + ",\"recallAtK\":" + number(recall / queryCount)
                + ",\"exactP50Millis\":" + number(percentile(exactLatencies, 0.50) / 1_000_000.0)
                + ",\"exactP95Millis\":" + number(percentile(exactLatencies, 0.95) / 1_000_000.0)
                + ",\"exactP99Millis\":" + number(percentile(exactLatencies, 0.99) / 1_000_000.0)
                + ",\"hnswP50Millis\":" + number(percentile(annLatencies, 0.50) / 1_000_000.0)
                + ",\"hnswP95Millis\":" + number(percentile(annLatencies, 0.95) / 1_000_000.0)
                + ",\"hnswP99Millis\":" + number(percentile(annLatencies, 0.99) / 1_000_000.0) + "}";
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
        for (String part : value.split(",")) sizes.add(Integer.parseInt(part.trim()));
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
