package com.nebula.search;

/** Deterministic hook for repeatable network-shard failure experiments. */
public interface ShardFaultInjector {
    void beforeAttempt(String shardId, int attempt) throws Exception;

    static ShardFaultInjector none() {
        return new ShardFaultInjector() {
            @Override public void beforeAttempt(String shardId, int attempt) { }
        };
    }
}
