package com.nebula.evaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Query-level retrieval errors for one ranking variant. */
public final class QueryErrorAnalysis {
    private final String variant;
    private final String queryId;
    private final List<String> missedRelevantSources;
    private final List<String> unexpectedSources;
    private final Map<String, Integer> returnedRanks;

    public QueryErrorAnalysis(String variant, String queryId, List<String> missedRelevantSources,
                              List<String> unexpectedSources, Map<String, Integer> returnedRanks) {
        this.variant = variant;
        this.queryId = queryId;
        this.missedRelevantSources = Collections.unmodifiableList(new ArrayList<>(missedRelevantSources));
        this.unexpectedSources = Collections.unmodifiableList(new ArrayList<>(unexpectedSources));
        this.returnedRanks = Collections.unmodifiableMap(new LinkedHashMap<>(returnedRanks));
    }

    public String getVariant() { return variant; }
    public String getQueryId() { return queryId; }
    public List<String> getMissedRelevantSources() { return missedRelevantSources; }
    public List<String> getUnexpectedSources() { return unexpectedSources; }
    public Map<String, Integer> getReturnedRanks() { return returnedRanks; }
}
