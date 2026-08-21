package com.nebula.search;

/** Thread-safe circuit-breaker state and retry counters for one shard endpoint. */
public final class ShardHealth {
    private enum State { CLOSED, OPEN, HALF_OPEN }
    private final int failureThreshold;
    private final long cooldownMillis;
    private State state = State.CLOSED;
    private int consecutiveFailures;
    private long totalAttempts;
    private long totalSuccesses;
    private long totalFailures;
    private long lastFailureEpochMillis;

    public ShardHealth(int failureThreshold, long cooldownMillis) {
        if (failureThreshold <= 0 || cooldownMillis <= 0) throw new IllegalArgumentException("circuit settings must be positive");
        this.failureThreshold = failureThreshold; this.cooldownMillis = cooldownMillis;
    }
    public synchronized boolean allowRequest() {
        if (state != State.OPEN) { totalAttempts++; return true; }
        if (System.currentTimeMillis() - lastFailureEpochMillis >= cooldownMillis) {
            state = State.HALF_OPEN; totalAttempts++; return true;
        }
        return false;
    }
    public synchronized void recordSuccess() { totalSuccesses++; consecutiveFailures = 0; state = State.CLOSED; }
    public synchronized void recordFailure() {
        totalFailures++; consecutiveFailures++; lastFailureEpochMillis = System.currentTimeMillis();
        if (consecutiveFailures >= failureThreshold) state = State.OPEN;
    }
    public synchronized Snapshot snapshot() {
        return new Snapshot(state.name(), consecutiveFailures, totalAttempts, totalSuccesses, totalFailures, lastFailureEpochMillis);
    }

    public static final class Snapshot {
        private final String state; private final int consecutiveFailures; private final long totalAttempts;
        private final long totalSuccesses; private final long totalFailures; private final long lastFailureEpochMillis;
        private Snapshot(String state, int consecutiveFailures, long totalAttempts, long totalSuccesses, long totalFailures, long lastFailureEpochMillis) {
            this.state = state; this.consecutiveFailures = consecutiveFailures; this.totalAttempts = totalAttempts;
            this.totalSuccesses = totalSuccesses; this.totalFailures = totalFailures; this.lastFailureEpochMillis = lastFailureEpochMillis;
        }
        public String getState() { return state; }
        public int getConsecutiveFailures() { return consecutiveFailures; }
        public long getTotalAttempts() { return totalAttempts; }
        public long getTotalSuccesses() { return totalSuccesses; }
        public long getTotalFailures() { return totalFailures; }
        public long getLastFailureEpochMillis() { return lastFailureEpochMillis; }
    }
}
