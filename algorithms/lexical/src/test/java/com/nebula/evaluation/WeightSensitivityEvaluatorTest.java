package com.nebula.evaluation;

import java.util.List;

public final class WeightSensitivityEvaluatorTest {
    public static void main(String[] args) {
        List<double[]> configurations = WeightSensitivityEvaluator.configurations(new double[]{0.35, 0.35, 0.10, 0.10, 0.10});
        check(configurations.size() == 16, "baseline plus 15 one-factor configurations");
        for (double[] configuration : configurations) {
            double sum = 0.0; for (double value : configuration) sum += value;
            check(Math.abs(sum - 1.0) < 0.000001, "weights are normalized");
        }
        System.out.println("WeightSensitivityEvaluatorTest: PASS");
    }

    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
