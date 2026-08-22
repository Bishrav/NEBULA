package com.nebula.search;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Coordinates independent HTTP shard endpoints with bounded client retries. */
public final class NetworkQueryCoordinator {
    private final List<HttpShardClient> shards;
    public NetworkQueryCoordinator(List<HttpShardClient> shards) {
        if (shards == null || shards.isEmpty()) throw new IllegalArgumentException("at least one HTTP shard is required");
        this.shards = new ArrayList<>(shards);
    }
    public NetworkSearchResponse search(String query, int limit) {
        Map<String, SearchResult> merged = new LinkedHashMap<>(); List<String> queried = new ArrayList<>(); List<String> failed = new ArrayList<>();
        for (HttpShardClient shard : shards) {
            queried.add(shard.getShardId());
            try {
                for (SearchResult result : shard.search(query, Math.max(limit, limit * 2))) {
                    String id = result.getDocument().getDocumentId(); SearchResult previous = merged.get(id);
                    if (previous == null || result.getScore() > previous.getScore()) merged.put(id, result);
                }
            } catch (Exception failure) { failed.add(shard.getShardId()); }
        }
        List<SearchResult> results = new ArrayList<>(merged.values());
        results.sort(new Comparator<SearchResult>() { @Override public int compare(SearchResult left, SearchResult right) {
            int score = Double.compare(right.getScore(), left.getScore());
            return score != 0 ? score : left.getDocument().getSourcePath().compareTo(right.getDocument().getSourcePath());
        }});
        return new NetworkSearchResponse(results.subList(0, Math.min(limit, results.size())), queried, failed);
    }
}
