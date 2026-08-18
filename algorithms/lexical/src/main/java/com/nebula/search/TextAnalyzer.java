package com.nebula.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/** Deterministic baseline analyzer; stemming and stop-word policies come later. */
public final class TextAnalyzer {
    private static final Pattern SEPARATOR = Pattern.compile("[^\\p{L}\\p{Nd}]+");

    private TextAnalyzer() { }

    public static List<String> analyze(String text) {
        if (text == null || text.trim().isEmpty()) return Collections.emptyList();
        String normalized = text.toLowerCase(Locale.ROOT).trim();
        String[] rawTerms = SEPARATOR.split(normalized);
        List<String> terms = new ArrayList<>();
        for (String term : rawTerms) {
            if (!term.isEmpty()) terms.add(term);
        }
        return Collections.unmodifiableList(terms);
    }
}
