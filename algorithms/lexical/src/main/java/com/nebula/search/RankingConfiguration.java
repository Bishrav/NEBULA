package com.nebula.search;

import java.util.Locale;

/** Immutable, serializable weights for a reproducible ranking experiment. */
public final class RankingConfiguration {
    private final String schemaVersion;
    private final String fusionStrategy;
    private final double lexicalWeight;
    private final double semanticWeight;
    private final double authorityWeight;
    private final double freshnessWeight;
    private final double graphWeight;
    private final double freshnessHalfLifeDays;

    private RankingConfiguration(String fusionStrategy, double lexicalWeight, double semanticWeight,
                                 double authorityWeight, double freshnessWeight, double graphWeight,
                                 double freshnessHalfLifeDays) {
        if (fusionStrategy == null || fusionStrategy.trim().isEmpty()) throw new IllegalArgumentException("fusion strategy is required");
        if (freshnessHalfLifeDays <= 0.0) throw new IllegalArgumentException("freshness half-life must be positive");
        double total = lexicalWeight + semanticWeight + authorityWeight + freshnessWeight + graphWeight;
        if (lexicalWeight < 0.0 || semanticWeight < 0.0 || authorityWeight < 0.0
                || freshnessWeight < 0.0 || graphWeight < 0.0 || total <= 0.0) {
            throw new IllegalArgumentException("ranking weights must be non-negative and have a positive sum");
        }
        this.schemaVersion = "ranking-configuration-v1";
        this.fusionStrategy = fusionStrategy;
        this.lexicalWeight = lexicalWeight / total;
        this.semanticWeight = semanticWeight / total;
        this.authorityWeight = authorityWeight / total;
        this.freshnessWeight = freshnessWeight / total;
        this.graphWeight = graphWeight / total;
        this.freshnessHalfLifeDays = freshnessHalfLifeDays;
    }

    public static RankingConfiguration hybrid(double lexicalWeight, double semanticWeight) {
        return new RankingConfiguration("weighted_min_max", lexicalWeight, semanticWeight, 0.0, 0.0, 0.0, 30.0);
    }

    /** Compatibility constructor: the authority channel is split into metadata 70% and graph 30%. */
    public static RankingConfiguration trustAware(double lexicalWeight, double authorityWeight,
                                                  double freshnessWeight, double freshnessHalfLifeDays) {
        return new RankingConfiguration("weighted_observable_signals", lexicalWeight, 0.0,
                authorityWeight * 0.70, freshnessWeight, authorityWeight * 0.30, freshnessHalfLifeDays);
    }

    public static RankingConfiguration trustAwareComponents(double lexicalWeight, double authorityWeight,
                                                            double freshnessWeight, double graphWeight,
                                                            double freshnessHalfLifeDays) {
        return new RankingConfiguration("weighted_observable_signals", lexicalWeight, 0.0,
                authorityWeight, freshnessWeight, graphWeight, freshnessHalfLifeDays);
    }

    public static RankingConfiguration trustOrientedHybrid(double lexicalWeight, double semanticWeight,
                                                           double authorityWeight, double freshnessWeight,
                                                           double graphWeight, double freshnessHalfLifeDays) {
        return new RankingConfiguration("weighted_observable_signals", lexicalWeight, semanticWeight,
                authorityWeight, freshnessWeight, graphWeight, freshnessHalfLifeDays);
    }

    public String getSchemaVersion() { return schemaVersion; }
    public String getFusionStrategy() { return fusionStrategy; }
    public double getLexicalWeight() { return lexicalWeight; }
    public double getSemanticWeight() { return semanticWeight; }
    public double getAuthorityWeight() { return authorityWeight; }
    public double getFreshnessWeight() { return freshnessWeight; }
    public double getGraphWeight() { return graphWeight; }
    public double getFreshnessHalfLifeDays() { return freshnessHalfLifeDays; }

    /** Stable JSON without external dependencies, intended for experiment manifests and logs. */
    public String toJson() {
        return String.format(Locale.ROOT,
                "{\"schemaVersion\":\"%s\",\"fusionStrategy\":\"%s\",\"lexicalWeight\":%.8f,\"semanticWeight\":%.8f,\"authorityWeight\":%.8f,\"freshnessWeight\":%.8f,\"graphWeight\":%.8f,\"freshnessHalfLifeDays\":%.8f}",
                schemaVersion, fusionStrategy, lexicalWeight, semanticWeight, authorityWeight,
                freshnessWeight, graphWeight, freshnessHalfLifeDays);
    }
}
