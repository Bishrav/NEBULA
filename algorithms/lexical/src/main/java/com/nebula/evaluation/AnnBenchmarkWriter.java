package com.nebula.evaluation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/** Writes the exact-vs-ANN evaluation artifact. */
public final class AnnBenchmarkWriter {
    public void writeMarkdown(Path target, AnnBenchmark.Result result) throws IOException {
        if (target.getParent() != null) Files.createDirectories(target.getParent());
        String body = "# NEBULA ANN Benchmark\n\n"
                + "> Exact cosine retrieval is the correctness baseline. Latency is a local engineering measurement, not a publication claim.\n\n"
                + "- Corpus documents: " + result.getDocuments() + "\n"
                + "- Queries: " + result.getQueries() + "\n"
                + "- Cutoff: @" + result.getCutoff() + "\n"
                + "- HNSW maximum level: " + result.getMaxLevel() + "\n\n"
                + "| Method | Recall@k | Mean latency (ms) |\n| --- | ---: | ---: |\n"
                + "| Exact cosine | 1.0000 | " + number(result.getExactLatencyMs()) + " |\n"
                + "| HNSW | " + number(result.getRecall()) + " | " + number(result.getAnnLatencyMs()) + " |\n\n"
                + "Keep corpus, query set, embedding model, cutoff, and HNSW parameters fixed when comparing revisions.\n";
        Files.write(target, body.getBytes(StandardCharsets.UTF_8));
    }

    public void writeJson(Path target, AnnBenchmark.Result result) throws IOException {
        if (target.getParent() != null) Files.createDirectories(target.getParent());
        String body = "{\"schemaVersion\":\"ann-evaluation-v1\",\"documents\":" + result.getDocuments()
                + ",\"queries\":" + result.getQueries() + ",\"cutoff\":" + result.getCutoff()
                + ",\"hnswMaxLevel\":" + result.getMaxLevel() + ",\"exactRecallAtK\":1.0"
                + ",\"hnswRecallAtK\":" + number(result.getRecall())
                + ",\"exactMeanLatencyMs\":" + number(result.getExactLatencyMs())
                + ",\"hnswMeanLatencyMs\":" + number(result.getAnnLatencyMs()) + "}\n";
        Files.write(target, body.getBytes(StandardCharsets.UTF_8));
    }

    private static String number(double value) { return String.format(Locale.ROOT, "%.6f", value); }
}
