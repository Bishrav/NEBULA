package com.nebula.search;

/** Tests session search telemetry aggregation. */
public final class SearchMetricsTest {
    public static void main(String[] args) {
        SearchMetrics metrics = new SearchMetrics();
        metrics.record("bm25", 2, 10_000_000L);
        metrics.record("hybrid", 0, 20_000_000L);
        check(metrics.getTotalSearches() == 2, "searches are counted");
        check(metrics.getZeroResultSearches() == 1, "zero-result searches are counted");
        check(metrics.getAverageLatencyMillis() == 15.0, "average latency is calculated");
        check(metrics.getSearchesByMode().get("hybrid") == 1, "mode counts are tracked");
        System.out.println("SearchMetricsTest: PASS");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
