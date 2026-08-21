package com.nebula.search;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Read-only health view for configured network shard clients. */
public final class ShardHealthMonitor {
    private final List<HttpShardClient> clients;
    public ShardHealthMonitor(List<HttpShardClient> clients) {
        if (clients == null) throw new IllegalArgumentException("clients are required");
        this.clients = clients;
    }
    public Map<String, ShardHealth.Snapshot> snapshot() {
        Map<String, ShardHealth.Snapshot> result = new LinkedHashMap<>();
        for (HttpShardClient client : clients) result.put(client.getShardId(), client.health().snapshot());
        return Collections.unmodifiableMap(result);
    }
}
