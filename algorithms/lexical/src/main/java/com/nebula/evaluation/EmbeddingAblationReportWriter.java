package com.nebula.evaluation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/** Writes a durable Markdown report for embedding ablation experiments. */
public final class EmbeddingAblationReportWriter {
    public void write(Path target, int corpusDocuments, int queryCount,
                      EmbeddingAblationReport ablation) throws IOException {
        if (target.getParent() != null) Files.createDirectories(target.getParent());
        StringBuilder report = new StringBuilder();
        report.append("# NEBULA Embedding Ablation Report\n\n");
        report.append("> Controlled comparison of deterministic embedding baselines. This is an engineering experiment, not a publication claim.\n\n");
        report.append("- Corpus documents: ").append(corpusDocuments).append('\n');
        report.append("- Labelled queries: ").append(queryCount).append('\n');
        report.append("- Embedding dimension: 128\n\n");
        report.append("## Results\n\n");
        report.append("| Variant | Precision | Recall | MRR | NDCG |\n");
        report.append("| --- | ---: | ---: | ---: | ---: |\n");
        for (String variant : ablation.getReports().keySet()) {
            EvaluationReport metrics = ablation.get(variant);
            report.append('|').append(variant).append('|')
                    .append(number(metrics.getMeanPrecisionAtK())).append('|')
                    .append(number(metrics.getMeanRecallAtK())).append('|')
                    .append(number(metrics.getMeanReciprocalRank())).append('|')
                    .append(number(metrics.getMeanNdcgAtK())).append("|\n");
        }
        report.append("\n## Interpretation\n\n");
        report.append("Compare `hashing:semantic` with `char_ngram:semantic` to isolate embedding behaviour. Compare the two `:hybrid` variants to measure whether embedding changes affect fused retrieval. Keep the dataset and cutoff fixed before drawing conclusions.\n");
        Files.write(target, report.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static String number(double value) { return String.format(Locale.ROOT, "%.4f", value); }
}
