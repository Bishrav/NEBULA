package com.nebula.evaluation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/** Writes machine-readable and human-readable graph health artifacts. */
public final class GraphBenchmarkWriter {
    public void writeMarkdown(Path target, GraphBenchmark.Result result) throws IOException {
        if (target.getParent() != null) Files.createDirectories(target.getParent());
        String body = "# NEBULA Graph Benchmark\n\n"
                + "> Deterministic PageRank health report for the indexed document graph.\n\n"
                + "| Measure | Value |\n| --- | ---: |\n"
                + "| Nodes | " + result.getNodes() + " |\n"
                + "| Directed edges | " + result.getEdges() + " |\n"
                + "| Dangling nodes | " + result.getDangling() + " |\n"
                + "| Iterations | " + result.getIterations() + " |\n"
                + "| Converged | " + result.isConverged() + " |\n"
                + "| Score mass | " + number(result.getScoreMass()) + " |\n\n"
                + "Top nodes by PageRank: " + String.join(", ", result.getTopNodes()) + "\n";
        Files.write(target, body.getBytes(StandardCharsets.UTF_8));
    }

    public void writeJson(Path target, GraphBenchmark.Result result) throws IOException {
        if (target.getParent() != null) Files.createDirectories(target.getParent());
        StringBuilder body = new StringBuilder("{\"schemaVersion\":\"graph-evaluation-v1\",\"nodes\":")
                .append(result.getNodes()).append(",\"edges\":").append(result.getEdges())
                .append(",\"danglingNodes\":").append(result.getDangling())
                .append(",\"iterations\":").append(result.getIterations())
                .append(",\"converged\":").append(result.isConverged())
                .append(",\"scoreMass\":").append(number(result.getScoreMass())).append(",\"topNodes\":[");
        for (int i = 0; i < result.getTopNodes().size(); i++) {
            if (i > 0) body.append(',');
            body.append('"').append(escape(result.getTopNodes().get(i))).append('"');
        }
        body.append("]}\n");
        Files.write(target, body.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static String number(double value) { return String.format(Locale.ROOT, "%.6f", value); }
    private static String escape(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\""); }
}
