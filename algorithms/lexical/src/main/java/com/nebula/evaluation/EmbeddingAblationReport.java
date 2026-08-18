package com.nebula.evaluation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Evaluation reports for controlled embedding-model comparisons. */
public final class EmbeddingAblationReport {
    private final Map<String, EvaluationReport> reports;

    public EmbeddingAblationReport(Map<String, EvaluationReport> reports) {
        this.reports = Collections.unmodifiableMap(new LinkedHashMap<>(reports));
    }

    public Map<String, EvaluationReport> getReports() { return reports; }
    public EvaluationReport get(String variant) { return reports.get(variant); }
}
