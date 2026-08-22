package com.nebula.search;

import com.nebula.ingestion.DocumentRecord;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

/** Tests rank-based fusion independently of lexical and semantic score scales. */
public final class ReciprocalRankFusionTest {
    public static void main(String[] args) {
        DocumentRecord first = new DocumentRecord("a", "a.md", "test", "A", "A", "a");
        DocumentRecord second = new DocumentRecord("b", "b.md", "test", "B", "B", "b");
        SearchResult a = new SearchResult(first, 1000.0, new LinkedHashMap<String, Double>());
        SearchResult b = new SearchResult(second, 0.1, new LinkedHashMap<String, Double>());
        List<SearchResult> result = new ReciprocalRankFusion(1).fuse(
                Arrays.asList(Arrays.asList(a, b), Collections.singletonList(b)), 2);
        check(result.get(0).getDocument().getDocumentId().equals("b"), "consensus rank wins over incompatible scores");
        check(Math.abs(result.get(0).getScore() - (1.0 / 3.0 + 0.5)) < 0.000001, "RRF score follows formula");
        check(result.get(0).getTermContributions().containsKey("signal:rrf"), "RRF explanation is exposed");
        System.out.println("ReciprocalRankFusionTest: PASS");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
