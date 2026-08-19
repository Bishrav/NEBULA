package com.nebula.search;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Tests deterministic Markdown directory loading for restart-safe local runs. */
public final class SearchCatalogDirectoryLoadTest {
    public static void main(String[] args) throws Exception {
        Path directory = Files.createTempDirectory("nebula-corpus-");
        try {
            Files.write(directory.resolve("b.md"), "# B\n\nShard failure runbook.".getBytes(StandardCharsets.UTF_8));
            Files.write(directory.resolve("a.md"), "# A\n\nDeployment rollback procedure.".getBytes(StandardCharsets.UTF_8));
            SearchCatalog catalog = new SearchCatalog();
            check(catalog.indexMarkdownDirectory(directory) == 2, "all Markdown documents are loaded");
            List<SearchResult> results = catalog.search("deployment rollback", 5);
            check(results.size() == 1, "loaded corpus is searchable");
            check(results.get(0).getDocument().getSourcePath().equals("a.md"), "relative source path is stable");
            System.out.println("SearchCatalogDirectoryLoadTest: PASS");
        } finally {
            Files.deleteIfExists(directory.resolve("a.md"));
            Files.deleteIfExists(directory.resolve("b.md"));
            Files.deleteIfExists(directory);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
