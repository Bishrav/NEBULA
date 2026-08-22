package com.nebula.search;

import java.util.Arrays;

/** Tests HTTP shard querying, result merging, and bounded failure handling. */
public final class NetworkQueryCoordinatorTest {
    public static void main(String[] args) throws Exception {
        SearchCatalog catalog = new SearchCatalog();
        catalog.indexMarkdown("docs/architecture.md", "# Architecture\n\nThe coordinator merges distributed shard results.");
        LexicalSearchHttpServer server = LexicalSearchHttpServer.create(0, catalog); server.start();
        try {
            HttpShardClient live = new HttpShardClient("live", "http://127.0.0.1:" + server.getPort(), 1000, 2);
            HttpShardClient unavailable = new HttpShardClient("offline", "http://127.0.0.1:1", 100, 2);
            NetworkSearchResponse response = new NetworkQueryCoordinator(Arrays.asList(live, unavailable)).search("coordinator", 5);
            check(response.getResults().size() == 1, "live HTTP shard contributes result");
            check(response.isPartial() && response.getFailedShards().contains("offline"), "offline shard is reported");
            System.out.println("NetworkQueryCoordinatorTest: PASS");
        } finally { server.stop(); }
    }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
