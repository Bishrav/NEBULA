package com.nebula.search;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** In-memory search telemetry for local product and research sessions. */
public final class SearchMetrics {
    private int totalSearches;
    private int zeroResultSearches;
    private long totalLatencyNanos;
    private final Map<String, Integer> searchesByMode = new LinkedHashMap<>();

    public synchronized void record(String mode, int resultCount, long latencyNanos) {
        if (mode == null || mode.trim().isEmpty()) throw new IllegalArgumentException("mode must not be blank");
        totalSearches++;
        if (resultCount == 0) zeroResultSearches++;
        totalLatencyNanos += Math.max(0L, latencyNanos);
        searchesByMode.put(mode, searchesByMode.containsKey(mode) ? searchesByMode.get(mode) + 1 : 1);
    }

    public synchronized int getTotalSearches() { return totalSearches; }
    public synchronized int getZeroResultSearches() { return zeroResultSearches; }
    public synchronized double getAverageLatencyMillis() {
        return totalSearches == 0 ? 0.0 : totalLatencyNanos / 1_000_000.0 / totalSearches;
    }
    public synchronized double getTotalLatencySeconds() {
        return totalLatencyNanos / 1_000_000_000.0;
    }
    public synchronized Map<String, Integer> getSearchesByMode() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(searchesByMode));
    }
}
