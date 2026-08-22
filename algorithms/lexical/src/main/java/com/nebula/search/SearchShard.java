package com.nebula.search;

import java.util.List;

/** Local shard boundary used by the coordinator and failure simulation. */
public final class SearchShard {
    private final String shardId;
    private final SearchCatalog catalog;
    private boolean available = true;
    public SearchShard(String shardId) {
        if (shardId == null || shardId.trim().isEmpty()) throw new IllegalArgumentException("shardId is required");
        this.shardId = shardId; this.catalog = new SearchCatalog();
    }
    public String getShardId() { return shardId; }
    public synchronized void index(String sourcePath, String content) { catalog.indexMarkdown(sourcePath, content); }
    public synchronized List<SearchResult> search(String query, int limit) {
        if (!available) throw new IllegalStateException("shard is unavailable: " + shardId);
        return catalog.search(query, limit);
    }
    public synchronized boolean isAvailable() { return available; }
    public synchronized void setAvailable(boolean available) { this.available = available; }
}
