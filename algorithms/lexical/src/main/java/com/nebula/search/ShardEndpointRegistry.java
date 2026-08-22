package com.nebula.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Static startup registry for shard IDs and HTTP endpoints. */
public final class ShardEndpointRegistry {
    private final List<HttpShardClient> clients;
    private ShardEndpointRegistry(List<HttpShardClient> clients) { this.clients = Collections.unmodifiableList(new ArrayList<>(clients)); }
    public static ShardEndpointRegistry parse(String value, int timeoutMillis, int maxAttempts, String apiToken) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException("shard endpoints are required");
        List<HttpShardClient> clients = new ArrayList<>();
        for (String entry : value.split(",")) {
            String[] parts = entry.trim().split("=", 2);
            if (parts.length != 2 || parts[0].trim().isEmpty() || parts[1].trim().isEmpty()) throw new IllegalArgumentException("invalid shard endpoint: " + entry);
            clients.add(new HttpShardClient(parts[0].trim(), parts[1].trim(), timeoutMillis, maxAttempts, apiToken));
        }
        return new ShardEndpointRegistry(clients);
    }
    public List<HttpShardClient> clients() { return clients; }
}
