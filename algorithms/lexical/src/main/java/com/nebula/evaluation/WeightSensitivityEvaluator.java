package com.nebula.evaluation;

import com.nebula.ingestion.DocumentRecord;
import com.nebula.search.SearchCatalog;
import com.nebula.search.SearchResult;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** One-factor-at-a-time sensitivity grid for the trust-oriented ranking weights. */
public final class WeightSensitivityEvaluator {
    private static final long NOW = 1_700_000_000_000L;
    private WeightSensitivityEvaluator() { }

    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.err.println("Usage: WeightSensitivityEvaluator <corpus> <queries.psv> <trust.psv> <output.json> [cutoff] [nowEpochMillis] [--embedding-cache cache.tsv] [--graph-links graph.psv]");
            System.exit(2);
        }
        Path embeddingCache = null;
        Path graphLinks = null;
        List<String> positional = new ArrayList<>();
        for (int index = 0; index < args.length; index++) {
            if ("--embedding-cache".equals(args[index])) {
                if (++index >= args.length) throw new IllegalArgumentException("--embedding-cache requires a path");
                embeddingCache = Paths.get(args[index]);
            } else if ("--graph-links".equals(args[index])) {
                if (++index >= args.length) throw new IllegalArgumentException("--graph-links requires a path");
                graphLinks = Paths.get(args[index]);
            } else positional.add(args[index]);
        }
        if (positional.size() < 4 || positional.size() > 6) throw new IllegalArgumentException("invalid positional arguments");
        Path corpus = Paths.get(positional.get(0)); Path queriesPath = Paths.get(positional.get(1)); Path trust = Paths.get(positional.get(2)); Path output = Paths.get(positional.get(3));
        int cutoff = positional.size() > 4 ? Integer.parseInt(positional.get(4)) : 5;
        long now = positional.size() > 5 ? Long.parseLong(positional.get(5)) : NOW;
        if (cutoff <= 0) throw new IllegalArgumentException("cutoff must be positive");
        SearchCatalog catalog = embeddingCache == null
                ? new SearchCatalog()
                : new SearchCatalog(new com.nebula.ingestion.DocumentIngestor(), new com.nebula.search.InvertedIndex(), new com.nebula.search.CachedEmbeddingModel(embeddingCache));
        List<DocumentRecord> documents = new ArrayList<>();
        try (java.util.stream.Stream<Path> stream = Files.walk(corpus)) {
            List<Path> files = stream.filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".md"))
                    .sorted(Comparator.comparing(path -> path.toString())).toList();
            for (Path file : files) {
                String relative = corpus.relativize(file).toString().replace('\\', '/');
                documents.add(catalog.indexMarkdown(relative, Files.readString(file, StandardCharsets.UTF_8)));
            }
        }
        for (com.nebula.search.DocumentTrustMetadata metadata : new TrustMetadataDatasetLoader().load(trust, now)) catalog.registerTrustMetadata(metadata);
        if (graphLinks != null) {
            for (String line : Files.readAllLines(graphLinks, StandardCharsets.UTF_8)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("source_path|")) continue;
                String[] columns = trimmed.split("\\|", 2);
                if (columns.length != 2) throw new IllegalArgumentException("invalid graph link row: " + line);
                List<String> targets = columns[1].trim().isEmpty()
                        ? java.util.Collections.emptyList()
                        : java.util.Arrays.asList(columns[1].split(";"));
                catalog.registerGraphLinks(columns[0], targets);
            }
        }
        List<EvaluationQuery> queries = new EvaluationDatasetLoader().load(queriesPath);
        double[] baseline = {0.35, 0.35, 0.10, 0.10, 0.10};
        StringBuilder json = new StringBuilder("{\"schemaVersion\":\"weight-sensitivity-v1\",\"evidenceStatus\":\"REGRESSION_FIXTURE_ONLY\",\"corpusDocuments\":")
                .append(documents.size()).append(",\"queryCount\":").append(queries.size()).append(",\"cutoff\":").append(cutoff)
                .append(",\"evaluationNowEpochMillis\":").append(now).append(",\"results\":[");
        List<double[]> configurations = configurations(baseline);
        for (int index = 0; index < configurations.size(); index++) {
            if (index > 0) json.append(',');
            double[] weights = configurations.get(index);
            List<EvaluationMetrics> metrics = new ArrayList<>();
            for (EvaluationQuery query : queries) {
                List<SearchResult> results = catalog.searchTrustOrientedHybrid(query.getText(), cutoff, now,
                        weights[0], weights[1], weights[2], weights[3], weights[4]);
                metrics.add(EvaluationMetricCalculator.calculate(query, results, cutoff));
            }
            json.append(String.format(Locale.ROOT, "{\"lexicalWeight\":%.6f,\"semanticWeight\":%.6f,\"authorityWeight\":%.6f,\"freshnessWeight\":%.6f,\"graphWeight\":%.6f,\"meanPrecisionAtK\":%.6f,\"meanRecallAtK\":%.6f,\"meanMRR\":%.6f,\"meanNDCGAtK\":%.6f}",
                    weights[0], weights[1], weights[2], weights[3], weights[4], mean(metrics, 0), mean(metrics, 1), mean(metrics, 2), mean(metrics, 3)));
        }
        json.append("]}\n");
        if (output.getParent() != null) Files.createDirectories(output.getParent());
        Files.writeString(output, json.toString(), StandardCharsets.UTF_8);
        System.out.println("GENERATED: " + output.toAbsolutePath());
    }

    static List<double[]> configurations(double[] baseline) {
        List<double[]> result = new ArrayList<>(); result.add(baseline.clone());
        for (int changed = 0; changed < baseline.length; changed++) {
            for (double value : new double[]{0.0, 0.20, 0.50}) {
                double[] next = baseline.clone(); double remaining = 1.0 - value; double oldRemaining = 1.0 - baseline[changed];
                next[changed] = value;
                for (int index = 0; index < next.length; index++) if (index != changed) next[index] = oldRemaining == 0.0 ? remaining / 4.0 : baseline[index] * remaining / oldRemaining;
                result.add(next);
            }
        }
        return result;
    }

    private static double mean(List<EvaluationMetrics> metrics, int metric) {
        if (metrics.isEmpty()) return 0.0; double total = 0.0;
        for (EvaluationMetrics item : metrics) total += metric == 0 ? item.getPrecisionAtK() : metric == 1 ? item.getRecallAtK() : metric == 2 ? item.getReciprocalRank() : item.getNdcgAtK();
        return total / metrics.size();
    }
}
