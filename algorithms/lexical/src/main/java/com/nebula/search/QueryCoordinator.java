package com.nebula.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Fans a query out to shards and merges local top-k results deterministically. */
public final class QueryCoordinator {
    private final List<SearchShard> shards;
    public QueryCoordinator(List<SearchShard> shards) {
        if (shards == null || shards.isEmpty()) throw new IllegalArgumentException("at least one shard is required");
        this.shards = Collections.unmodifiableList(new ArrayList<>(shards));
    }
    public CoordinatedSearchResponse search(String query, int limit) {
        if (query == null || query.trim().isEmpty()) throw new IllegalArgumentException("query must not be blank");
        if (limit <= 0) throw new IllegalArgumentException("limit must be positive");
        List<String> queried = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        Map<String, SearchResult> merged = new LinkedHashMap<>();
        for (SearchShard shard : shards) {
            queried.add(shard.getShardId());
            try {
                for (SearchResult result : shard.search(query, Math.max(limit, limit * 2))) {
                    String id = result.getDocument().getDocumentId();
                    SearchResult previous = merged.get(id);
                    if (previous == null || result.getScore() > previous.getScore()) merged.put(id, result);
                }
            } catch (RuntimeException exception) { failed.add(shard.getShardId()); }
        }
        List<SearchResult> results = new ArrayList<>(merged.values());
        results.sort(new Comparator<SearchResult>() {
            @Override public int compare(SearchResult left, SearchResult right) {
                int score = Double.compare(right.getScore(), left.getScore());
                return score != 0 ? score : left.getDocument().getSourcePath().compareTo(right.getDocument().getSourcePath());
            }
        });
        return new CoordinatedSearchResponse(results.subList(0, Math.min(limit, results.size())), queried, failed);
    }
}
