package com.nebula.search;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Fans out across replicated shard groups and retries a failed primary on its replica. */
public final class ResilientQueryCoordinator {
    private final List<ReplicatedSearchShard> shards;
    public ResilientQueryCoordinator(List<ReplicatedSearchShard> shards) {
        if (shards == null || shards.isEmpty()) throw new IllegalArgumentException("at least one shard is required");
        this.shards = new ArrayList<>(shards);
    }
    public ResilientSearchResponse search(String query, int limit) {
        if (query == null || query.trim().isEmpty()) throw new IllegalArgumentException("query must not be blank");
        if (limit <= 0) throw new IllegalArgumentException("limit must be positive");
        Map<String, SearchResult> merged = new LinkedHashMap<>();
        List<String> recovered = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        for (ReplicatedSearchShard shard : shards) {
            try {
                ReplicatedSearchShard.ReplicatedSearchResponse response = shard.search(query, Math.max(limit, limit * 2));
                if (response.isRecovered()) recovered.add(shard.getShardId());
                for (SearchResult result : response.getResults()) {
                    String id = result.getDocument().getDocumentId();
                    SearchResult previous = merged.get(id);
                    if (previous == null || result.getScore() > previous.getScore()) merged.put(id, result);
                }
            } catch (RuntimeException failure) { failed.add(shard.getShardId()); }
        }
        List<SearchResult> results = new ArrayList<>(merged.values());
        results.sort(new Comparator<SearchResult>() {
            @Override public int compare(SearchResult left, SearchResult right) {
                int score = Double.compare(right.getScore(), left.getScore());
                return score != 0 ? score : left.getDocument().getSourcePath().compareTo(right.getDocument().getSourcePath());
            }
        });
        return new ResilientSearchResponse(results.subList(0, Math.min(limit, results.size())), recovered, failed);
    }
}
