package com.nebula.evaluation;

public final class BgeHybridSweepEvaluatorTest {
    public static void main(String[] args) {
        check(BgeHybridSweepEvaluator.configurations().size() == 20, "five weights across four candidate depths");
        for (BgeHybridSweepEvaluator.SweepConfiguration configuration : BgeHybridSweepEvaluator.configurations()) {
            check(Math.abs(configuration.lexicalWeight + configuration.semanticWeight - 1.0) < 0.000001,
                    "weights sum to one");
            check(configuration.candidateMultiplier > 0, "candidate multiplier is positive");
        }
        System.out.println("BgeHybridSweepEvaluatorTest: PASS");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
