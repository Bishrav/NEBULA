package com.nebula.evaluation;

import com.nebula.ingestion.DocumentRecord;
import com.nebula.search.CachedEmbeddingModel;
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

/** Reproducible lexical/semantic weight and candidate-depth sweep for a pinned cache. */
public final class BgeHybridSweepEvaluator {
    private static final long NOW = 1_700_000_000_000L;
    private BgeHybridSweepEvaluator() { }

    public static void main(String[] args) throws Exception {
        if (args.length < 4 || args.length > 5) {
            System.err.println("Usage: BgeHybridSweepEvaluator <corpus> <queries.psv> <embedding-cache> <output.json> [cutoff]");
            System.exit(2);
        }
        Path corpus = Paths.get(args[0]);
        Path queriesPath = Paths.get(args[1]);
        Path cache = Paths.get(args[2]);
        Path output = Paths.get(args[3]);
        int cutoff = args.length == 5 ? Integer.parseInt(args[4]) : 5;
        if (cutoff <= 0) throw new IllegalArgumentException("cutoff must be positive");

        CachedEmbeddingModel model = new CachedEmbeddingModel(cache);
        SearchCatalog catalog = new SearchCatalog(new com.nebula.ingestion.DocumentIngestor(),
                new com.nebula.search.InvertedIndex(), model);
        int documents = index(catalog, corpus);
        List<EvaluationQuery> queries = new EvaluationDatasetLoader().load(queriesPath);
        StringBuilder json = new StringBuilder("{\"schemaVersion\":\"bge-hybrid-sweep-v1\",\"modelId\":\"")
                .append(escape(model.modelId())).append("\",\"embeddingDimension\":").append(model.dimension())
                .append(",\"corpusDocuments\":").append(documents).append(",\"queryCount\":").append(queries.size())
                .append(",\"cutoff\":").append(cutoff).append(",\"results\":[");
        List<SweepConfiguration> configurations = configurations();
        for (int index = 0; index < configurations.size(); index++) {
            if (index > 0) json.append(',');
            SweepConfiguration configuration = configurations.get(index);
            List<EvaluationMetrics> metrics = new ArrayList<>();
            for (EvaluationQuery query : queries) {
                List<SearchResult> results = catalog.hybridSearch(query.getText(), cutoff,
                        configuration.lexicalWeight, configuration.semanticWeight,
                        configuration.candidateMultiplier);
                metrics.add(EvaluationMetricCalculator.calculate(query, results, cutoff));
            }
            json.append(String.format(Locale.ROOT,
                    "{\"lexicalWeight\":%.2f,\"semanticWeight\":%.2f,\"candidateMultiplier\":%d,\"meanPrecisionAtK\":%.6f,\"meanRecallAtK\":%.6f,\"meanMRR\":%.6f,\"meanNDCGAtK\":%.6f}",
                    configuration.lexicalWeight, configuration.semanticWeight, configuration.candidateMultiplier,
                    mean(metrics, 0), mean(metrics, 1), mean(metrics, 2), mean(metrics, 3)));
        }
        json.append("]}\n");
        if (output.getParent() != null) Files.createDirectories(output.getParent());
        Files.writeString(output, json.toString(), StandardCharsets.UTF_8);
        System.out.println("model=" + model.modelId() + "|documents=" + documents + "|queries=" + queries.size());
        System.out.println("GENERATED: " + output.toAbsolutePath());
    }

    static List<SweepConfiguration> configurations() {
        List<SweepConfiguration> result = new ArrayList<>();
        double[] lexicalWeights = {0.25, 0.40, 0.50, 0.60, 0.75};
        int[] candidateMultipliers = {3, 5, 10, 20};
        for (int multiplier : candidateMultipliers) {
            for (double lexical : lexicalWeights) result.add(new SweepConfiguration(lexical, 1.0 - lexical, multiplier));
        }
        return result;
    }

    private static int index(SearchCatalog catalog, Path corpus) throws Exception {
        List<Path> files;
        try (java.util.stream.Stream<Path> stream = Files.walk(corpus)) {
            files = stream.filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".md"))
                    .sorted(Comparator.comparing(path -> path.toString())).toList();
        }
        for (Path file : files) {
            String relative = corpus.relativize(file).toString().replace('\\', '/');
            catalog.indexMarkdown(relative, Files.readString(file, StandardCharsets.UTF_8));
        }
        return files.size();
    }

    private static double mean(List<EvaluationMetrics> metrics, int metric) {
        if (metrics.isEmpty()) return 0.0;
        double total = 0.0;
        for (EvaluationMetrics item : metrics) {
            total += metric == 0 ? item.getPrecisionAtK() : metric == 1 ? item.getRecallAtK()
                    : metric == 2 ? item.getReciprocalRank() : item.getNdcgAtK();
        }
        return total / metrics.size();
    }

    private static String escape(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\""); }

    static final class SweepConfiguration {
        final double lexicalWeight;
        final double semanticWeight;
        final int candidateMultiplier;

        SweepConfiguration(double lexicalWeight, double semanticWeight, int candidateMultiplier) {
            this.lexicalWeight = lexicalWeight;
            this.semanticWeight = semanticWeight;
            this.candidateMultiplier = candidateMultiplier;
        }
    }
}
