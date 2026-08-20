package com.nebula.search;

import com.nebula.ingestion.DocumentIngestor;

/** Tests graph extraction, dangling-node handling, convergence, and authority ordering. */
public final class PageRankTest {
    public static void main(String[] args) {
        DocumentIngestor ingestor = new DocumentIngestor();
        LinkGraph graph = new LinkGraph();
        graph.add(ingestor.ingest("a.md", "# A\n\n[B](b.md)"));
        graph.add(ingestor.ingest("b.md", "# B\n\n[B](b.md) [C](c.md)"));
        graph.add(ingestor.ingest("c.md", "# C\n\n"));

        PageRankResult result = PageRank.compute(graph);
        check(graph.nodes().size() == 3, "all linked nodes are present");
        check(graph.edgeCount() == 3, "unique directed edges are counted");
        check(result.isConverged(), "PageRank converges");
        check(result.score("b.md") > result.score("a.md"), "linked authority raises B");
        check(result.topNodes(1).get(0).equals("b.md"), "highest authority node is ranked first");
        System.out.println("PageRankTest: PASS");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
