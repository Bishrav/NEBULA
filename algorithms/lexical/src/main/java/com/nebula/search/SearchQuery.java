package com.nebula.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parsed lexical query containing free terms and exact quoted phrases. */
public final class SearchQuery {
    private static final Pattern QUOTED_PHRASE = Pattern.compile("\\\"([^\\\"]+)\\\"");
    private final List<String> terms;
    private final List<String> freeTerms;
    private final List<List<String>> phrases;

    private SearchQuery(List<String> terms, List<String> freeTerms, List<List<String>> phrases) {
        this.terms = Collections.unmodifiableList(new ArrayList<>(terms));
        this.freeTerms = Collections.unmodifiableList(new ArrayList<>(freeTerms));
        this.phrases = Collections.unmodifiableList(new ArrayList<>(phrases));
    }

    public static SearchQuery parse(String rawQuery) {
        if (rawQuery == null || rawQuery.trim().isEmpty()) {
            return new SearchQuery(Collections.<String>emptyList(), Collections.<String>emptyList(),
                    Collections.<List<String>>emptyList());
        }
        Matcher matcher = QUOTED_PHRASE.matcher(rawQuery);
        StringBuffer remaining = new StringBuffer();
        List<List<String>> phrases = new ArrayList<>();
        while (matcher.find()) {
            List<String> phraseTerms = TextAnalyzer.analyze(matcher.group(1));
            if (!phraseTerms.isEmpty()) phrases.add(phraseTerms);
            matcher.appendReplacement(remaining, " ");
        }
        matcher.appendTail(remaining);

        List<String> freeTerms = TextAnalyzer.analyze(remaining.toString());
        Set<String> uniqueFreeTerms = new LinkedHashSet<>(freeTerms);
        Set<String> uniqueTerms = new LinkedHashSet<>(uniqueFreeTerms);
        for (List<String> phrase : phrases) uniqueTerms.addAll(phrase);
        return new SearchQuery(new ArrayList<>(uniqueTerms), new ArrayList<>(uniqueFreeTerms), phrases);
    }

    public List<String> getTerms() { return terms; }
    public List<String> getFreeTerms() { return freeTerms; }
    public List<List<String>> getPhrases() { return phrases; }
}
