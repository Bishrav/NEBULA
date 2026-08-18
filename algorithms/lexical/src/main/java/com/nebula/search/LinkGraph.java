package com.nebula.search;

import com.nebula.ingestion.DocumentRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Directed document-link graph extracted during ingestion. */
public final class LinkGraph {
    private final Map<String, Set<String>> outgoingBySource = new LinkedHashMap<>();

    public synchronized void add(DocumentRecord document) {
        Set<String> outgoing = outgoingBySource.get(document.getSourcePath());
        if (outgoing == null) {
            outgoing = new LinkedHashSet<>();
            outgoingBySource.put(document.getSourcePath(), outgoing);
        }
        outgoing.addAll(document.getLinks());
        for (String target : document.getLinks()) {
            if (!outgoingBySource.containsKey(target)) outgoingBySource.put(target, new LinkedHashSet<String>());
        }
    }

    public synchronized Set<String> nodes() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(outgoingBySource.keySet()));
    }

    public synchronized Set<String> outgoing(String source) {
        Set<String> outgoing = outgoingBySource.get(source);
        return outgoing == null
                ? Collections.<String>emptySet()
                : Collections.unmodifiableSet(new LinkedHashSet<>(outgoing));
    }

    public synchronized int edgeCount() {
        int edges = 0;
        for (Set<String> outgoing : outgoingBySource.values()) edges += outgoing.size();
        return edges;
    }
}
