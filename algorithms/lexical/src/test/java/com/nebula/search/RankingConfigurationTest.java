package com.nebula.search;

/** Regression tests for normalized, serializable research ranking configurations. */
public final class RankingConfigurationTest {
    public static void main(String[] args) {
        RankingConfiguration hybrid = RankingConfiguration.hybrid(2.0, 1.0);
        check(Math.abs(hybrid.getLexicalWeight() - (2.0 / 3.0)) < 0.000001, "hybrid weights normalize");
        check(Math.abs(hybrid.getSemanticWeight() - (1.0 / 3.0)) < 0.000001, "semantic weight normalizes");
        check(hybrid.toJson().contains("ranking-configuration-v1"), "configuration serializes schema");

        RankingConfiguration trust = RankingConfiguration.trustAware(0.70, 0.20, 0.10, 30.0);
        double sum = trust.getLexicalWeight() + trust.getSemanticWeight() + trust.getAuthorityWeight()
                + trust.getFreshnessWeight() + trust.getGraphWeight();
        check(Math.abs(sum - 1.0) < 0.000001, "trust weights sum to one");
        check(Math.abs(trust.getAuthorityWeight() - 0.14) < 0.000001, "metadata authority split is explicit");
        check(Math.abs(trust.getGraphWeight() - 0.06) < 0.000001, "graph authority split is explicit");
        check(trust.toJson().contains("weighted_observable_signals"), "trust strategy serializes");
        System.out.println("RankingConfigurationTest: PASS");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
