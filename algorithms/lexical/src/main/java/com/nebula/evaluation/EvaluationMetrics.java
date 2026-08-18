package com.nebula.evaluation;

/** Metrics for one query at a requested cutoff. */
public final class EvaluationMetrics {
    private final String queryId;
    private final double precisionAtK;
    private final double recallAtK;
    private final double reciprocalRank;
    private final double ndcgAtK;

    public EvaluationMetrics(String queryId, double precisionAtK, double recallAtK,
                             double reciprocalRank, double ndcgAtK) {
        this.queryId = queryId;
        this.precisionAtK = precisionAtK;
        this.recallAtK = recallAtK;
        this.reciprocalRank = reciprocalRank;
        this.ndcgAtK = ndcgAtK;
    }

    public String getQueryId() { return queryId; }
    public double getPrecisionAtK() { return precisionAtK; }
    public double getRecallAtK() { return recallAtK; }
    public double getReciprocalRank() { return reciprocalRank; }
    public double getNdcgAtK() { return ndcgAtK; }
}
