package com.nebula.search;

import java.util.Arrays;

/** Tests synchronous replication, primary failover, and total shard failure. */
public final class ReplicationRecoveryTest {
    public static void main(String[] args) {
        ReplicatedSearchShard shard = new ReplicatedSearchShard("shard-a");
        shard.index("docs/operations.md", "# Operations\n\nA replica serves results after a primary failure.");
        ResilientQueryCoordinator coordinator = new ResilientQueryCoordinator(Arrays.asList(shard));
        ResilientSearchResponse healthy = coordinator.search("replica", 5);
        check(healthy.getResults().size() == 1 && healthy.getRecoveredShards().isEmpty(), "primary serves healthy query");

        shard.setPrimaryAvailable(false);
        ResilientSearchResponse recovered = coordinator.search("replica", 5);
        check(recovered.getResults().size() == 1, "replica serves recovered query");
        check(recovered.getRecoveredShards().contains("shard-a"), "recovery is reported");
        check(!recovered.isPartial(), "replica recovery is complete");

        shard.setReplicaAvailable(false);
        ResilientSearchResponse failed = coordinator.search("replica", 5);
        check(failed.isPartial(), "total replica failure is partial");
        check(failed.getFailedShards().contains("shard-a"), "failed shard group is reported");
        check(failed.getResults().isEmpty(), "unavailable group contributes no results");
        System.out.println("ReplicationRecoveryTest: PASS");
    }

    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
