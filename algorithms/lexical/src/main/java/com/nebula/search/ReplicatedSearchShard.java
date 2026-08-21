package com.nebula.search;

import java.util.List;

/** Primary/replica shard pair with synchronous indexing and query failover. */
public final class ReplicatedSearchShard {
    private final String shardId;
    private final SearchShard primary;
    private final SearchShard replica;

    public ReplicatedSearchShard(String shardId) {
        if (shardId == null || shardId.trim().isEmpty()) throw new IllegalArgumentException("shardId is required");
        this.shardId = shardId;
        this.primary = new SearchShard(shardId + "-primary");
        this.replica = new SearchShard(shardId + "-replica");
    }

    public String getShardId() { return shardId; }
    public synchronized void index(String sourcePath, String content) {
        primary.index(sourcePath, content);
        replica.index(sourcePath, content);
    }
    public synchronized ReplicatedSearchResponse search(String query, int limit) {
        try {
            return new ReplicatedSearchResponse(primary.search(query, limit), primary.getShardId(), false);
        } catch (RuntimeException primaryFailure) {
            try {
                return new ReplicatedSearchResponse(replica.search(query, limit), replica.getShardId(), true);
            } catch (RuntimeException replicaFailure) {
                throw new IllegalStateException("both replicas are unavailable: " + shardId, replicaFailure);
            }
        }
    }
    public void setPrimaryAvailable(boolean available) { primary.setAvailable(available); }
    public void setReplicaAvailable(boolean available) { replica.setAvailable(available); }
    public boolean isPrimaryAvailable() { return primary.isAvailable(); }
    public boolean isReplicaAvailable() { return replica.isAvailable(); }

    public static final class ReplicatedSearchResponse {
        private final List<SearchResult> results;
        private final String servingNode;
        private final boolean recovered;
        private ReplicatedSearchResponse(List<SearchResult> results, String servingNode, boolean recovered) {
            this.results = results; this.servingNode = servingNode; this.recovered = recovered;
        }
        public List<SearchResult> getResults() { return results; }
        public String getServingNode() { return servingNode; }
        public boolean isRecovered() { return recovered; }
    }
}
