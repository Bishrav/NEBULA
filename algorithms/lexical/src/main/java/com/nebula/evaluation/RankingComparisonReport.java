package com.nebula.evaluation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Evaluation reports for multiple ranking variants. */
public final class RankingComparisonReport {
    private final Map<String, EvaluationReport> reports;

    public RankingComparisonReport(Map<String, EvaluationReport> reports) {
        this.reports = Collections.unmodifiableMap(new LinkedHashMap<>(reports));
    }

    public Set<String> getVariants() { return reports.keySet(); }
    public EvaluationReport get(String variant) { return reports.get(variant); }

    public double precisionDelta(String variant, String baseline) {
        return get(variant).getMeanPrecisionAtK() - get(baseline).getMeanPrecisionAtK();
    }

    public double recallDelta(String variant, String baseline) {
        return get(variant).getMeanRecallAtK() - get(baseline).getMeanRecallAtK();
    }

    public double mrrDelta(String variant, String baseline) {
        return get(variant).getMeanReciprocalRank() - get(baseline).getMeanReciprocalRank();
    }

    public double ndcgDelta(String variant, String baseline) {
        return get(variant).getMeanNdcgAtK() - get(baseline).getMeanNdcgAtK();
    }
}
