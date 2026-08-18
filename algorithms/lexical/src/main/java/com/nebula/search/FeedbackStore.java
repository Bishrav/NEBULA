package com.nebula.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** In-memory feedback store for local product discovery sessions. */
public final class FeedbackStore {
    private final List<FeedbackRecord> records = new ArrayList<>();

    public synchronized void record(FeedbackRecord feedback) {
        if (feedback == null) throw new IllegalArgumentException("feedback must not be null");
        records.add(feedback);
    }

    public synchronized List<FeedbackRecord> records() {
        return Collections.unmodifiableList(new ArrayList<>(records));
    }

    public synchronized int total() { return records.size(); }

    public synchronized int usefulCount() {
        int count = 0;
        for (FeedbackRecord record : records) if (record.isUseful()) count++;
        return count;
    }

    public synchronized int notUsefulCount() { return total() - usefulCount(); }

    public synchronized double usefulRate() {
        return records.isEmpty() ? 0.0 : (double) usefulCount() / records.size();
    }
}
