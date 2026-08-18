package com.nebula.evaluation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Loads the versioned pipe-separated evaluation format used by benchmarks. */
public final class EvaluationDatasetLoader {
    public List<EvaluationQuery> load(Path source) throws IOException {
        List<EvaluationQuery> queries = new ArrayList<>();
        for (String line : Files.readAllLines(source, StandardCharsets.UTF_8)) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("query_id|")) continue;
            String[] columns = trimmed.split("\\|", 3);
            if (columns.length != 3) throw new IOException("invalid evaluation row: " + line);
            queries.add(new EvaluationQuery(columns[0], columns[1], parseJudgments(columns[2])));
        }
        return queries;
    }

    private static Map<String, Integer> parseJudgments(String encoded) throws IOException {
        Map<String, Integer> judgments = new LinkedHashMap<>();
        if (encoded.trim().isEmpty()) return judgments;
        for (String item : encoded.split(";")) {
            String[] pair = item.split(":", 2);
            if (pair.length != 2) throw new IOException("invalid judgment: " + item);
            try {
                judgments.put(pair[0], Integer.parseInt(pair[1]));
            } catch (NumberFormatException exception) {
                throw new IOException("invalid relevance grade: " + item, exception);
            }
        }
        return judgments;
    }
}
