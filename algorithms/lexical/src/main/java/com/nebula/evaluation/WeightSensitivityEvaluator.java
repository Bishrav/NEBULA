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
        if (args.length < 4 || args.length > 6) {
            System.err.println("Usage: WeightSensitivityEvaluator <corpus> <queries.psv> <trust.psv> <output.json> [cutoff] [nowEpochMillis]");
            System.exit(2);
        }
        Path corpus = Paths.get(args[0]); Path queriesPath = Paths.get(args[1]); Path trust = Paths.get(args[2]); Path output = Paths.get(args[3]);
        int cutoff = args.length > 4 ? Integer.parseInt(args[4]) : 5;
        long now = args.length > 5 ? Long.parseLong(args[5]) : NOW;
        if (cutoff <= 0) throw new IllegalArgumentException("cutoff must be positive");
        SearchCatalog catalog = new SearchCatalog();
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
