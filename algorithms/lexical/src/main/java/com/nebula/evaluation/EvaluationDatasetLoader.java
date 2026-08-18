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
        java.util.Set<String> queryIds = new java.util.HashSet<>();
        for (String line : Files.readAllLines(source, StandardCharsets.UTF_8)) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("query_id|")) continue;
            String[] columns = trimmed.split("\\|", 3);
            if (columns.length != 3) throw new IOException("invalid evaluation row: " + line);
            if (columns[0].trim().isEmpty() || columns[1].trim().isEmpty()) {
                throw new IOException("query id and query text must not be blank: " + line);
            }
            if (!queryIds.add(columns[0].trim())) throw new IOException("duplicate query id: " + columns[0]);
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
            if (pair[0].trim().isEmpty()) throw new IOException("judgement source must not be blank: " + item);
            try {
                if (judgments.containsKey(pair[0].trim())) throw new IOException("duplicate judgement: " + item);
                int grade = Integer.parseInt(pair[1].trim());
                if (grade < 0 || grade > 3) throw new IOException("relevance grade must be between 0 and 3: " + item);
                judgments.put(pair[0].trim(), grade);
            } catch (NumberFormatException exception) {
                throw new IOException("invalid relevance grade: " + item, exception);
            }
        }
        return judgments;
    }
}
