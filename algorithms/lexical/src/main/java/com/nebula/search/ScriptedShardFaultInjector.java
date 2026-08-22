package com.nebula.search;

import java.io.IOException;
import java.net.SocketTimeoutException;

/** Injects a fixed number of deterministic failures before allowing requests through. */
public final class ScriptedShardFaultInjector implements ShardFaultInjector {
    public enum Fault { LATENCY_TIMEOUT, HTTP_500, CONNECTION_REFUSED }

    private final Fault fault;
    private final int failures;
    private final long latencyMillis;
    private int injected;

    public ScriptedShardFaultInjector(Fault fault, int failures, long latencyMillis) {
        if (fault == null || failures < 0 || latencyMillis < 0) throw new IllegalArgumentException("fault settings are invalid");
        this.fault = fault;
        this.failures = failures;
        this.latencyMillis = latencyMillis;
    }

    @Override
    public synchronized void beforeAttempt(String shardId, int attempt) throws Exception {
        if (injected >= failures) return;
        injected++;
        if (fault == Fault.LATENCY_TIMEOUT) {
            if (latencyMillis > 0) Thread.sleep(latencyMillis);
            throw new SocketTimeoutException("injected timeout for shard " + shardId);
        }
        if (fault == Fault.HTTP_500) throw new IOException("injected HTTP 500 for shard " + shardId);
        throw new IOException("injected connection refusal for shard " + shardId);
    }
}
