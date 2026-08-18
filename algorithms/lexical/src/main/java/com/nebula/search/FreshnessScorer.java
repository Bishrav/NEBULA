package com.nebula.search;

/** Exponential freshness decay with a configurable half-life in days. */
public final class FreshnessScorer {
    private static final double MILLIS_PER_DAY = 86_400_000.0;
    private final double halfLifeDays;

    public FreshnessScorer(double halfLifeDays) {
        if (halfLifeDays <= 0.0) throw new IllegalArgumentException("halfLifeDays must be positive");
        this.halfLifeDays = halfLifeDays;
    }

    public double score(long lastVerifiedEpochMillis, long nowEpochMillis) {
        if (lastVerifiedEpochMillis >= nowEpochMillis) return 1.0;
        double ageDays = (nowEpochMillis - lastVerifiedEpochMillis) / MILLIS_PER_DAY;
        return Math.exp(-Math.log(2.0) * ageDays / halfLifeDays);
    }
}
