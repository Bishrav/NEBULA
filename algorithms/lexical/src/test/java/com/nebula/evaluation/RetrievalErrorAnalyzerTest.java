package com.nebula.evaluation;

import com.nebula.search.SearchCatalog;
import com.nebula.search.SearchResult;

import java.util.Collections;
import java.util.List;

/** Tests missed-relevant and unexpected-result detection. */
public final class RetrievalErrorAnalyzerTest {
    public static void main(String[] args) {
        SearchCatalog catalog = new SearchCatalog();
        catalog.indexMarkdown("docs/relevant.md", "# Distributed Search\n\nDistributed search uses shards.");
        catalog.indexMarkdown("docs/other.md", "# Deployment\n\nDistributed deployment uses health checks.");
        EvaluationQuery query = new EvaluationQuery("q1", "distributed search", Collections.singletonMap("docs/relevant.md", 3));
        List<SearchResult> results = catalog.search(query.getText(), 2);
        QueryErrorAnalysis analysis = new RetrievalErrorAnalyzer().analyze("bm25", query, results);
        check(analysis.getMissedRelevantSources().isEmpty(), "relevant source is found");
        check(analysis.getReturnedRanks().get("docs/relevant.md") == 1, "rank is recorded");
        check(analysis.getUnexpectedSources().contains("docs/other.md"), "unexpected source is recorded");
        System.out.println("RetrievalErrorAnalyzerTest: PASS");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
