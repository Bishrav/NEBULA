package com.nebula.evaluation;

/** Summary returned after validating a labelled evaluation dataset. */
public final class EvaluationDatasetValidationReport {
    private final int queryCount;
    private final int judgementCount;
    private final int relevantJudgementCount;

    public EvaluationDatasetValidationReport(int queryCount, int judgementCount, int relevantJudgementCount) {
        this.queryCount = queryCount;
        this.judgementCount = judgementCount;
        this.relevantJudgementCount = relevantJudgementCount;
    }

    public int getQueryCount() { return queryCount; }
    public int getJudgementCount() { return judgementCount; }
    public int getRelevantJudgementCount() { return relevantJudgementCount; }
}
