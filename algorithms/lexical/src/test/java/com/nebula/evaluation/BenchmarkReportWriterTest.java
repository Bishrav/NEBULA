package com.nebula.evaluation;

import com.nebula.search.SearchCatalog;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

/** Tests that benchmark reports are written with metrics and error sections. */
public final class BenchmarkReportWriterTest {
    public static void main(String[] args) throws Exception {
        SearchCatalog catalog = new SearchCatalog();
        catalog.indexMarkdown("docs/search.md", "# Search\n\nSearch ranking.");
        EvaluationQuery query = new EvaluationQuery("q1", "search", Collections.singletonMap("docs/search.md", 3));
        RankingComparisonReport comparison = new RankingVariantEvaluator().evaluate(
                catalog, Collections.singletonList(query), 1, 1_700_000_000_000L);
        java.util.List<QueryErrorAnalysis> errors = Collections.singletonList(new RetrievalErrorAnalyzer().analyze(
                "bm25", query, catalog.search("search", 1)));
        Path report = Files.createTempFile("nebula-report-", ".md");
        try {
            new BenchmarkReportWriter().write(report, 1, 1, comparison, errors);
            String content = new String(Files.readAllBytes(report), StandardCharsets.UTF_8);
            check(content.contains("Ranking comparison"), "report contains comparison");
            check(content.contains("Error analysis"), "report contains errors");
            check(content.contains("trust_aware"), "report contains trust variant");
            System.out.println("BenchmarkReportWriterTest: PASS");
        } finally {
            Files.deleteIfExists(report);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
