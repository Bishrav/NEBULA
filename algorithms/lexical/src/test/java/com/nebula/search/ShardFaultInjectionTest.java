package com.nebula.search;

import java.util.Arrays;

/** Tests deterministic injected failures without depending on manual container kills. */
public final class ShardFaultInjectionTest {
    public static void main(String[] args) throws Exception {
        SearchCatalog catalog = new SearchCatalog();
        catalog.indexMarkdown("docs/health.md", "# Health\n\nService health verification.");
        LexicalSearchHttpServer server = LexicalSearchHttpServer.create(0, catalog); server.start();
        try {
            HttpShardClient flaky = new HttpShardClient("flaky", "http://127.0.0.1:" + server.getPort(),
                    500, 3, null, 3, 1000,
                    new ScriptedShardFaultInjector(ScriptedShardFaultInjector.Fault.HTTP_500, 2, 0));
            NetworkSearchResponse response = new NetworkQueryCoordinator(Arrays.asList(flaky)).search("health", 1);
            check(!response.isPartial() && response.getResults().size() == 1, "transient injected failures recover");
            check(flaky.health().snapshot().getTotalFailures() == 2, "injected failures are counted");

            HttpShardClient broken = new HttpShardClient("broken", "http://127.0.0.1:" + server.getPort(),
                    500, 2, null, 2, 1000,
                    new ScriptedShardFaultInjector(ScriptedShardFaultInjector.Fault.CONNECTION_REFUSED, 2, 0));
            NetworkSearchResponse failed = new NetworkQueryCoordinator(Arrays.asList(broken)).search("health", 1);
            check(failed.isPartial() && failed.getFailedShards().contains("broken"), "persistent injected failure is partial");
            check("OPEN".equals(broken.health().snapshot().getState()), "persistent failure opens circuit");
            System.out.println("ShardFaultInjectionTest: PASS");
        } finally { server.stop(); }
    }

    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
