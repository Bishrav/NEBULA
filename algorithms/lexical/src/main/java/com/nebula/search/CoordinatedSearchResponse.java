package com.nebula.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Query-coordinator response with explicit partial-result state. */
public final class CoordinatedSearchResponse {
    private final List<SearchResult> results;
    private final List<String> queriedShards;
    private final List<String> failedShards;
    CoordinatedSearchResponse(List<SearchResult> results, List<String> queriedShards, List<String> failedShards) {
        this.results = Collections.unmodifiableList(new ArrayList<>(results));
        this.queriedShards = Collections.unmodifiableList(new ArrayList<>(queriedShards));
        this.failedShards = Collections.unmodifiableList(new ArrayList<>(failedShards));
    }
    public List<SearchResult> getResults() { return results; }
    public List<String> getQueriedShards() { return queriedShards; }
    public List<String> getFailedShards() { return failedShards; }
    public boolean isPartial() { return !failedShards.isEmpty(); }
}
