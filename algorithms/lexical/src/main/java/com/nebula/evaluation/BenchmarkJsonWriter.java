package com.nebula.evaluation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/** Writes a machine-readable, reproducible ranking comparison artifact. */
public final class BenchmarkJsonWriter {
    public void write(Path target, int corpusDocuments, int queryCount, long evaluationNow,
                      RankingComparisonReport comparison) throws IOException {
        if (target.getParent() != null) Files.createDirectories(target.getParent());
        StringBuilder json = new StringBuilder();
        json.append("{\"schemaVersion\":\"evaluation-v1\",\"corpusDocuments\":")
                .append(corpusDocuments).append(",\"queryCount\":").append(queryCount)
                .append(",\"cutoff\":").append(comparison.get("bm25").getCutoff())
                .append(",\"evaluationNowEpochMillis\":").append(evaluationNow)
                .append(",\"baseline\":\"bm25\",\"variants\":[");
        int index = 0;
        for (String variant : comparison.getVariants()) {
            if (index++ > 0) json.append(',');
            EvaluationReport report = comparison.get(variant);
            json.append("{\"name\":\"").append(escape(variant)).append("\",\"precisionAtK\":")
                    .append(number(report.getMeanPrecisionAtK())).append(",\"recallAtK\":")
                    .append(number(report.getMeanRecallAtK())).append(",\"mrr\":")
                    .append(number(report.getMeanReciprocalRank())).append(",\"ndcgAtK\":")
                    .append(number(report.getMeanNdcgAtK())).append(",\"deltaNdcgVsBm25\":")
                    .append(number("bm25".equals(variant) ? 0.0 : comparison.ndcgDelta(variant, "bm25")))
                    .append(",\"perQuery\":[");
            int queryIndex = 0;
            for (EvaluationMetrics query : report.getPerQuery()) {
                if (queryIndex++ > 0) json.append(',');
                json.append("{\"queryId\":\"").append(escape(query.getQueryId()))
                        .append("\",\"precisionAtK\":").append(number(query.getPrecisionAtK()))
                        .append(",\"recallAtK\":").append(number(query.getRecallAtK()))
                        .append(",\"mrr\":").append(number(query.getReciprocalRank()))
                        .append(",\"ndcgAtK\":").append(number(query.getNdcgAtK())).append('}');
            }
            json.append("]}");
        }
        json.append("]}\n");
        Files.write(target, json.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static String number(double value) { return String.format(Locale.ROOT, "%.6f", value); }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
