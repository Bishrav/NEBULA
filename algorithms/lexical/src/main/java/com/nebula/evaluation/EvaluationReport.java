package com.nebula.evaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Macro-averaged evaluation report with per-query details. */
public final class EvaluationReport {
    private final int cutoff;
    private final List<EvaluationMetrics> perQuery;

    public EvaluationReport(int cutoff, List<EvaluationMetrics> perQuery) {
        this.cutoff = cutoff;
        this.perQuery = Collections.unmodifiableList(new ArrayList<>(perQuery));
    }

    public int getCutoff() { return cutoff; }
    public List<EvaluationMetrics> getPerQuery() { return perQuery; }

    public double getMeanPrecisionAtK() { return mean(0); }
    public double getMeanRecallAtK() { return mean(1); }
    public double getMeanReciprocalRank() { return mean(2); }
    public double getMeanNdcgAtK() { return mean(3); }

    private double mean(int metric) {
        if (perQuery.isEmpty()) return 0.0;
        double total = 0.0;
        for (EvaluationMetrics result : perQuery) {
            if (metric == 0) total += result.getPrecisionAtK();
            else if (metric == 1) total += result.getRecallAtK();
            else if (metric == 2) total += result.getReciprocalRank();
            else total += result.getNdcgAtK();
        }
        return total / perQuery.size();
    }
}
