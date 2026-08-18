package com.nebula.search;

/** Tests local feedback aggregation for PMF discovery sessions. */
public final class FeedbackStoreTest {
    public static void main(String[] args) {
        FeedbackStore store = new FeedbackStore();
        store.record(new FeedbackRecord("shard failure", "hybrid", "doc-1", "docs/runbook.md", true));
        store.record(new FeedbackRecord("shard failure", "bm25", "doc-2", "docs/notes.md", false));
        check(store.total() == 2, "all feedback is counted");
        check(store.usefulCount() == 1, "useful feedback is counted");
        check(store.notUsefulCount() == 1, "not-useful feedback is counted");
        check(store.usefulRate() == 0.5, "useful rate is calculated");
        System.out.println("FeedbackStoreTest: PASS");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
