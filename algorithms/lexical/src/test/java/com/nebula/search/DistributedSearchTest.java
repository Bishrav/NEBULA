package com.nebula.search;

import java.util.Arrays;

/** Tests deterministic placement, fan-out merging, and partial-result reporting. */
public final class DistributedSearchTest {
    public static void main(String[] args) {
        ConsistentHashRing ring = new ConsistentHashRing(32);
        ring.addNode("shard-a"); ring.addNode("shard-b");
        String placement = ring.locate("docs/architecture.md");
        check(ring.nodes().size() == 2, "ring contains both shards");
        check(ring.locate("docs/architecture.md").equals(placement), "placement is deterministic");
        ring.removeNode("shard-b");
        check(ring.locate("docs/architecture.md").equals("shard-a"), "removal leaves surviving shard");

        SearchShard first = new SearchShard("shard-a");
        SearchShard second = new SearchShard("shard-b");
        first.index("docs/architecture.md", "# Architecture\n\nThe query coordinator merges shard results.");
        second.index("docs/operations.md", "# Operations\n\nThe shard reports a partial result after failure.");
        QueryCoordinator coordinator = new QueryCoordinator(Arrays.asList(first, second));
        CoordinatedSearchResponse complete = coordinator.search("shard", 5);
        check(complete.getResults().size() == 2, "coordinator merges shard results");
        check(!complete.isPartial(), "healthy fan-out is complete");
        second.setAvailable(false);
        CoordinatedSearchResponse partial = coordinator.search("shard", 5);
        check(partial.isPartial(), "failed shard produces partial response");
        check(partial.getFailedShards().contains("shard-b"), "failed shard is reported");
        check(partial.getResults().size() == 1, "partial response preserves available results");
        System.out.println("DistributedSearchTest: PASS");
    }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
