package com.nebula.search;

import java.nio.file.Files;
import java.nio.file.Path;

/** Verifies telemetry events survive closing and reopening the store. */
public final class PersistentTelemetryStoreTest {
    public static void main(String[] args) throws Exception {
        Path directory = Files.createTempDirectory("nebula-telemetry-test");
        Path file = directory.resolve("events.jsonl");
        PersistentTelemetryStore first = PersistentTelemetryStore.open(file);
        first.recordSearch("hybrid", 2, 12_000_000L);
        first.recordSearch("bm25", 0, 18_000_000L);
        first.recordFeedback(new FeedbackRecord("shard failure", "hybrid", "doc-1", "docs/runbook.md", true));

        PersistentTelemetryStore restored = PersistentTelemetryStore.open(file);
        check(restored.searchMetrics().getTotalSearches() == 2, "search events are restored");
        check(restored.searchMetrics().getZeroResultSearches() == 1, "zero-result events are restored");
        check(restored.searchMetrics().getSearchesByMode().get("hybrid") == 1, "mode counts are restored");
        check(restored.feedbackStore().total() == 1, "feedback events are restored");
        check(restored.feedbackStore().usefulCount() == 1, "useful feedback is restored");
        System.out.println("PersistentTelemetryStoreTest: PASS");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
