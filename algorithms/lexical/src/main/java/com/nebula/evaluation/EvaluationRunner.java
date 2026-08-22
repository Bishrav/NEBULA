package com.nebula.evaluation;

import com.nebula.search.SearchCatalog;
import com.nebula.ingestion.DocumentRecord;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/** Reproducible command-line evaluation over a corpus directory and query file. */
public final class EvaluationRunner {
    private static final long EVALUATION_NOW = 1_700_000_000_000L;

    private EvaluationRunner() { }

    public static void main(String[] args) throws Exception {
        boolean finalHeldout = false;
        java.util.List<String> positional = new java.util.ArrayList<>();
        for (String arg : args) { if ("--final-heldout-evaluation".equals(arg)) finalHeldout = true; else positional.add(arg); }
        if (positional.size() < 2 || positional.size() > 5) {
            System.err.println("Usage: EvaluationRunner <corpus-directory> <queries.psv> [trust-metadata.psv] [report.md] [comparison.json] [--final-heldout-evaluation]");
            System.exit(2);
        }
        Path corpus = Paths.get(positional.get(0));
        Path queryFile = Paths.get(positional.get(1));
        String queryName = queryFile.getFileName().toString().toLowerCase(java.util.Locale.ROOT);
        if ((queryName.contains("heldout") || queryName.contains("test")) && !finalHeldout) {
            throw new IllegalArgumentException("held-out/test evaluation requires explicit --final-heldout-evaluation");
        }
        SearchCatalog catalog = new SearchCatalog();
        List<DocumentRecord> corpusRecords = new ArrayList<>();
        List<Path> documents;
        try (java.util.stream.Stream<Path> stream = Files.walk(corpus)) {
            documents = stream.filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".md"))
                    .sorted(Comparator.comparing(path -> path.toString()))
                    .collect(Collectors.toList());
        }
        for (Path document : documents) {
            String sourcePath = corpus.relativize(document).toString().replace('\\', '/');
            corpusRecords.add(catalog.indexMarkdown(sourcePath,
                    new String(Files.readAllBytes(document), StandardCharsets.UTF_8)));
        }

        if (positional.size() >= 3) {
            for (com.nebula.search.DocumentTrustMetadata metadata
                    : new TrustMetadataDatasetLoader().load(Paths.get(positional.get(2)), EVALUATION_NOW)) {
                catalog.registerTrustMetadata(metadata);
            }
        }

        List<EvaluationQuery> queries = new EvaluationDatasetLoader().load(queryFile);
        Set<String> corpusSourcePaths = new LinkedHashSet<>();
        for (DocumentRecord record : corpusRecords) corpusSourcePaths.add(record.getSourcePath());
        EvaluationDatasetValidationReport validation = new EvaluationDatasetValidator()
                .validate(queries, corpusSourcePaths);
        System.out.println("corpus_documents=" + catalog.documentCount());
        System.out.println("queries=" + queries.size());
        System.out.println("judgements=" + validation.getJudgementCount());
        System.out.println("relevant_judgements=" + validation.getRelevantJudgementCount());
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
        if (positional.size() >= 4) {
            RankingVariantEvaluator variants = new RankingVariantEvaluator();
            RetrievalErrorAnalyzer analyzer = new RetrievalErrorAnalyzer();
            List<QueryErrorAnalysis> errors = new ArrayList<>();
            for (String variant : comparison.getVariants()) {
                for (EvaluationQuery query : queries) {
                    errors.add(analyzer.analyze(variant, query,
                            variants.searchVariant(catalog, query, 5, EVALUATION_NOW, variant)));
                }
            }
            new BenchmarkReportWriter().write(Paths.get(positional.get(3)), catalog.documentCount(), queries.size(), comparison, errors);
            Path embeddingReport = Paths.get(positional.get(3)).resolveSibling("embedding-ablation.md");
            EmbeddingAblationReport ablation = new EmbeddingAblationEvaluator()
                    .evaluate(corpusRecords, queries, 5);
            new EmbeddingAblationReportWriter().write(embeddingReport, catalog.documentCount(), queries.size(), ablation);
            AnnBenchmark.Result ann = new AnnBenchmark().evaluate(corpusRecords, queries, 5);
            Path annReport = Paths.get(positional.get(3)).resolveSibling("ann-benchmark.md");
            new AnnBenchmarkWriter().writeMarkdown(annReport, ann);
            if (positional.size() == 5) new AnnBenchmarkWriter().writeJson(Paths.get(positional.get(4)).resolveSibling("ann-benchmark.json"), ann);
            GraphBenchmark.Result graph = new GraphBenchmark().evaluate(corpusRecords);
            Path graphReport = Paths.get(positional.get(3)).resolveSibling("graph-benchmark.md");
            new GraphBenchmarkWriter().writeMarkdown(graphReport, graph);
            if (positional.size() == 5) new GraphBenchmarkWriter().writeJson(Paths.get(positional.get(4)).resolveSibling("graph-benchmark.json"), graph);
            System.out.println("report=" + Paths.get(positional.get(3)).toAbsolutePath());
            System.out.println("embedding_report=" + embeddingReport.toAbsolutePath());
            System.out.println("ann_report=" + annReport.toAbsolutePath());
            System.out.println("graph_report=" + graphReport.toAbsolutePath());
            if (positional.size() == 5) {
                Path jsonReport = Paths.get(positional.get(4));
                new BenchmarkJsonWriter().write(jsonReport, catalog.documentCount(), queries.size(),
                        EVALUATION_NOW, comparison);
                System.out.println("comparison_json=" + jsonReport.toAbsolutePath());
            }
        }
    }
}
