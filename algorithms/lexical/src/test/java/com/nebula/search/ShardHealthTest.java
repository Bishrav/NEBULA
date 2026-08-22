package com.nebula.search;

import java.util.Arrays;

/** Tests retry counters, circuit opening, half-open recovery, and monitoring snapshots. */
public final class ShardHealthTest {
    public static void main(String[] args) throws Exception {
        HttpShardClient offline = new HttpShardClient("offline", "http://127.0.0.1:1", 50, 2, null, 2, 60);
        try { offline.search("test", 1); } catch (Exception ignored) { }
        ShardHealth.Snapshot opened = offline.health().snapshot();
        check(opened.getTotalAttempts() == 1 && opened.getTotalFailures() == 2, "retry counters are recorded");
        check("OPEN".equals(opened.getState()), "circuit opens at threshold");
        try { offline.search("test", 1); } catch (Exception ignored) { }
        check(offline.health().snapshot().getTotalAttempts() == 1, "open circuit fails fast");

        ShardHealth direct = new ShardHealth(1, 1);
        check(direct.allowRequest(), "closed circuit allows request"); direct.recordFailure();
        check("OPEN".equals(direct.snapshot().getState()), "direct circuit opens");
        Thread.sleep(5);
        check(direct.allowRequest(), "cooldown enters half-open probe"); direct.recordSuccess();
        check("CLOSED".equals(direct.snapshot().getState()), "successful probe closes circuit");
        ShardHealthMonitor monitor = new ShardHealthMonitor(Arrays.asList(offline));
        check(monitor.snapshot().containsKey("offline"), "monitor reports configured shard");
        System.out.println("ShardHealthTest: PASS");
    }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
