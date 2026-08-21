package com.nebula.search;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Deterministic virtual-node consistent hash ring for document placement. */
public final class ConsistentHashRing {
    private final int virtualNodes;
    private final TreeMap<Long, String> ring = new TreeMap<>();

    public ConsistentHashRing(int virtualNodes) {
        if (virtualNodes <= 0) throw new IllegalArgumentException("virtualNodes must be positive");
        this.virtualNodes = virtualNodes;
    }
    public synchronized void addNode(String node) {
        requireNode(node); removeNode(node);
        for (int replica = 0; replica < virtualNodes; replica++) ring.put(hash(node + "#" + replica), node);
    }
    public synchronized void removeNode(String node) {
        if (node != null) ring.entrySet().removeIf(entry -> node.equals(entry.getValue()));
    }
    public synchronized String locate(String key) {
        if (ring.isEmpty()) throw new IllegalStateException("hash ring has no nodes");
        if (key == null || key.trim().isEmpty()) throw new IllegalArgumentException("key must not be blank");
        Map.Entry<Long, String> entry = ring.ceilingEntry(hash(key));
        return (entry == null ? ring.firstEntry() : entry).getValue();
    }
    public synchronized List<String> nodes() {
        List<String> result = new ArrayList<>();
        for (String node : ring.values()) if (!result.contains(node)) result.add(node);
        Collections.sort(result); return Collections.unmodifiableList(result);
    }
    private static void requireNode(String node) {
        if (node == null || node.trim().isEmpty()) throw new IllegalArgumentException("node must not be blank");
    }
    private static long hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            long result = 0L;
            for (int i = 0; i < 8; i++) result = (result << 8) | (digest[i] & 0xffL);
            return result & Long.MAX_VALUE;
        } catch (NoSuchAlgorithmException exception) { throw new IllegalStateException("SHA-256 is unavailable", exception); }
    }
}
