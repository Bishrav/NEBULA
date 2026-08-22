# Distributed evaluation plan

NEBULA's distributed components are evaluated as a systems-reliability secondary track. They do not substitute for the primary retrieval-quality study.

## Failure matrix

| Scenario | Controlled condition | Required observation |
| --- | --- | --- |
| Healthy | all shards and replicas available | complete result, baseline latency |
| Shard unavailable | one endpoint refuses connections | explicit partial response and failed-shard metadata |
| Primary failure | primary fails before response | replica recovery, retry count, failover event |
| Primary and replica failure | both endpoints fail | explicit partial response; no silent success |
| Slow shard | deterministic latency exceeds timeout | bounded request time and timeout accounting |
| Intermittent failure | first attempts fail, later attempt succeeds | recovery within retry budget |
| Repeated failure | failures exceed threshold | circuit opens and prevents unbounded retries |
| Recovery | cooldown expires and probe succeeds | circuit transitions back to healthy/closed |

## Implemented local controls

`ShardFaultInjector` and `ScriptedShardFaultInjector` provide deterministic latency, HTTP-500, and connection-refusal faults at the client boundary. `ShardFaultInjectionTest` verifies retry recovery and explicit partial results/circuit opening without manually killing processes.

This is a test harness, not production evidence. The multi-container topology, load generation, and hardware measurements remain **NOT YET MEASURED**.

## Metrics

Future runs must record throughput, p50/p95/p99 end-to-end latency, merge latency, per-shard latency, retry count, failed-shard count, replica failovers, circuit-open events, partial-result frequency, and result-quality degradation.

For healthy and degraded top-k results define:

```text
TopKOverlap = |K_healthy ∩ K_degraded| / k
```

Also report NDCG degradation against the same frozen qrels. Every run must include topology, timeout/retry configuration, fault schedule, seed, Git revision, and manifest checksums.

## Interpretation boundary

A successful retry test supports only the claim that the tested state transition works under the fixture. It does not demonstrate availability, tail-latency targets, scalability, or correctness under arbitrary network partitions.
