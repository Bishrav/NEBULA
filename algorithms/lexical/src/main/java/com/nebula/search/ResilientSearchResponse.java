package com.nebula.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Coordinator response exposing replica recovery and unrecoverable groups. */
public final class ResilientSearchResponse {
    private final List<SearchResult> results;
    private final List<String> recoveredShards;
    private final List<String> failedShards;
    ResilientSearchResponse(List<SearchResult> results, List<String> recoveredShards, List<String> failedShards) {
        this.results = Collections.unmodifiableList(new ArrayList<>(results));
        this.recoveredShards = Collections.unmodifiableList(new ArrayList<>(recoveredShards));
        this.failedShards = Collections.unmodifiableList(new ArrayList<>(failedShards));
    }
    public List<SearchResult> getResults() { return results; }
    public List<String> getRecoveredShards() { return recoveredShards; }
    public List<String> getFailedShards() { return failedShards; }
    public boolean isPartial() { return !failedShards.isEmpty(); }
}
