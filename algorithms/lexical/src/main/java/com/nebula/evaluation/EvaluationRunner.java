package com.nebula.evaluation;

import com.nebula.search.SearchCatalog;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** Reproducible command-line evaluation over a corpus directory and query file. */
public final class EvaluationRunner {
    private EvaluationRunner() { }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: EvaluationRunner <corpus-directory> <queries.psv>");
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

        List<EvaluationQuery> queries = new EvaluationDatasetLoader().load(queryFile);
        EvaluationReport report = new SearchEvaluator().evaluate(catalog, queries, 5);
        System.out.println("corpus_documents=" + catalog.documentCount());
        System.out.println("queries=" + queries.size());
        System.out.println("cutoff=" + report.getCutoff());
        System.out.println("precision_at_5=" + report.getMeanPrecisionAtK());
        System.out.println("recall_at_5=" + report.getMeanRecallAtK());
        System.out.println("mrr=" + report.getMeanReciprocalRank());
        System.out.println("ndcg_at_5=" + report.getMeanNdcgAtK());
        for (EvaluationMetrics metrics : report.getPerQuery()) {
            System.out.println(metrics.getQueryId() + "|precision=" + metrics.getPrecisionAtK()
                    + "|recall=" + metrics.getRecallAtK()
                    + "|mrr=" + metrics.getReciprocalRank()
                    + "|ndcg=" + metrics.getNdcgAtK());
        }
    }
}
