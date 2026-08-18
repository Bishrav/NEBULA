package com.nebula.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/** Prefix trie for frequency-ranked query suggestions. */
public final class AutocompleteTrie {
    private final Node root = new Node();

    public synchronized void addTerm(String rawTerm, int weight) {
        if (rawTerm == null || rawTerm.trim().isEmpty() || weight <= 0) return;
        String term = rawTerm.toLowerCase(Locale.ROOT).trim();
        Node current = root;
        for (char character : term.toCharArray()) {
            Node next = current.children.get(character);
            if (next == null) {
                next = new Node();
                current.children.put(character, next);
            }
            current = next;
        }
        current.term = term;
        current.frequency += weight;
    }

    public synchronized List<String> suggest(String rawPrefix, int limit) {
        if (limit <= 0) throw new IllegalArgumentException("limit must be positive");
        String prefix = rawPrefix == null ? "" : rawPrefix.toLowerCase(Locale.ROOT).trim();
        Node node = root;
        for (char character : prefix.toCharArray()) {
            node = node.children.get(character);
            if (node == null) return Collections.emptyList();
        }
        List<Suggestion> matches = new ArrayList<>();
        collect(node, matches);
        Collections.sort(matches, new Comparator<Suggestion>() {
            @Override
            public int compare(Suggestion left, Suggestion right) {
                int frequencyOrder = Integer.compare(right.frequency, left.frequency);
                return frequencyOrder != 0 ? frequencyOrder : left.term.compareTo(right.term);
            }
        });
        List<String> result = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, matches.size()); i++) result.add(matches.get(i).term);
        return Collections.unmodifiableList(result);
    }

    private void collect(Node node, List<Suggestion> matches) {
        if (node.term != null) matches.add(new Suggestion(node.term, node.frequency));
        for (Node child : node.children.values()) collect(child, matches);
    }

    private static final class Node {
        private final Map<Character, Node> children = new TreeMap<>();
        private String term;
        private int frequency;
    }

    private static final class Suggestion {
        private final String term;
        private final int frequency;

        private Suggestion(String term, int frequency) {
            this.term = term;
            this.frequency = frequency;
        }
    }
}
