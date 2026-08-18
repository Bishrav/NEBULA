package com.nebula.evaluation;

import com.nebula.search.SearchCatalog;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** Reproducible command-line evaluation over a corpus directory and query file. */
public final class EvaluationRunner {
    private static final long EVALUATION_NOW = 1_700_000_000_000L;

    private EvaluationRunner() { }

    public static void main(String[] args) throws Exception {
        if (args.length < 2 || args.length > 4) {
            System.err.println("Usage: EvaluationRunner <corpus-directory> <queries.psv> [trust-metadata.psv] [report.md]");
            System.exit(2);
        }
        Path corpus = Paths.get(args[0]);
        Path queryFile = Paths.get(args[1]);
        SearchCatalog catalog = new SearchCatalog();
        List<Path> documents;
        try (java.util.stream.Stream<Path> stream = Files.walk(corpus)) {
            documents = stream.filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".md"))
                    .sorted(Comparator.comparing(path -> path.toString()))
                    .collect(Collectors.toList());
        }
        for (Path document : documents) {
            String sourcePath = corpus.relativize(document).toString().replace('\\', '/');
            catalog.indexMarkdown(sourcePath, new String(Files.readAllBytes(document), StandardCharsets.UTF_8));
        }

        if (args.length == 3) {
            for (com.nebula.search.DocumentTrustMetadata metadata
                    : new TrustMetadataDatasetLoader().load(Paths.get(args[2]), EVALUATION_NOW)) {
                catalog.registerTrustMetadata(metadata);
            }
        }

        List<EvaluationQuery> queries = new EvaluationDatasetLoader().load(queryFile);
        System.out.println("corpus_documents=" + catalog.documentCount());
        System.out.println("queries=" + queries.size());
        RankingComparisonReport comparison = new RankingVariantEvaluator()
                .evaluate(catalog, queries, 5, EVALUATION_NOW);
        for (String variant : comparison.getVariants()) {
            EvaluationReport report = comparison.get(variant);
            System.out.println(variant + "|precision_at_5=" + report.getMeanPrecisionAtK()
                    + "|recall_at_5=" + report.getMeanRecallAtK()
                    + "|mrr=" + report.getMeanReciprocalRank()
                    + "|ndcg_at_5=" + report.getMeanNdcgAtK());
        }
        for (String variant : comparison.getVariants()) {
            if (!"bm25".equals(variant)) {
                System.out.println(variant + "_delta_vs_bm25"
                        + "|precision=" + comparison.precisionDelta(variant, "bm25")
                        + "|recall=" + comparison.recallDelta(variant, "bm25")
                        + "|mrr=" + comparison.mrrDelta(variant, "bm25")
                        + "|ndcg=" + comparison.ndcgDelta(variant, "bm25"));
            }
        }
        if (args.length == 4) {
            RankingVariantEvaluator variants = new RankingVariantEvaluator();
            RetrievalErrorAnalyzer analyzer = new RetrievalErrorAnalyzer();
            List<QueryErrorAnalysis> errors = new ArrayList<>();
            for (String variant : comparison.getVariants()) {
                for (EvaluationQuery query : queries) {
                    errors.add(analyzer.analyze(variant, query,
                            variants.searchVariant(catalog, query, 5, EVALUATION_NOW, variant)));
                }
            }
            new BenchmarkReportWriter().write(Paths.get(args[3]), catalog.documentCount(), queries.size(), comparison, errors);
            System.out.println("report=" + Paths.get(args[3]).toAbsolutePath());
        }
    }
}
