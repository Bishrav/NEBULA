package com.nebula.search;

import java.util.Arrays;
import java.util.List;

/** Tests prefix traversal, frequency ordering, and deterministic tie-breaking. */
public final class AutocompleteTrieTest {
    public static void main(String[] args) {
        AutocompleteTrie trie = new AutocompleteTrie();
        trie.addTerm("search", 3);
        trie.addTerm("search", 2);
        trie.addTerm("service", 4);
        trie.addTerm("semantic", 4);

        List<String> results = trie.suggest("sea", 3);
        check(results.equals(Arrays.asList("semantic", "service", "search")), "frequency and lexical ordering work");
        check(trie.suggest("xyz", 10).isEmpty(), "unknown prefixes return no suggestions");
        System.out.println("AutocompleteTrieTest: PASS");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
