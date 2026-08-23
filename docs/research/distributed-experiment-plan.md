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

This is a test harness, not production evidence. The dedicated topology is infrastructure/docker/compose.research-distributed.yaml. It contains one research coordinator and three primary/replica shard pairs. The coordinator exposes /v1/search and /v1/metrics; the shard service has deterministic NEBULA_FAULT_MODE, NEBULA_FAULT_DELAY_MS, and NEBULA_FAULT_EVERY controls that are inactive by default.

Run observations with tools/run_distributed_experiment.py. The runner records raw JSON, CSV, and Markdown outputs and keeps fault control outside the measurement process. Scenario definitions are versioned in experiments/distributed/scenario-matrix.json.

Before starting the topology, run `python tools/distributed_preflight.py`. A
`READY` result permits Compose startup. `ENVIRONMENT_LIMITED` means Docker
Desktop or its daemon is unavailable; it is an environment blocker, not a
distributed-system result.

## Metrics

Runs must record throughput, p50/p95/p99 end-to-end latency, server latency, retry attempts, failed-shard count, open circuits, partial-result frequency, and result-quality degradation. The current coordinator exposes aggregate shard health counters; replica failover counts and per-shard latency require an additional instrumented deployment if those measures become primary claims.

For healthy and degraded top-k results define:

```text
TopKOverlap = |K_healthy ∩ K_degraded| / k
```

Also report NDCG degradation against the same frozen qrels. Every run must include topology, timeout/retry configuration, fault schedule, query, repeat count, Git revision, and manifest checksums. If qrels are unavailable, record **NOT MEASURED**.

## Interpretation boundary

A successful retry test supports only the claim that the tested state transition works under the fixture. It does not demonstrate availability, tail-latency targets, scalability, or correctness under arbitrary network partitions.

## Local measurement snapshot

The first local Docker run used the six-node topology, Java 17 containers, the
default 500 ms shard timeout, two attempts, and a `shard failure` query. D0
healthy completed 10 repetitions with no partial responses or retries. D1, with
one primary stopped, completed three repetitions with partial-result frequency
`1.0`, two scenario-local retries, and top-k overlap `0.4`. D3, with one full
primary/replica pair stopped, completed three repetitions with partial-result
frequency `1.0`, 18 scenario-local retries, and open circuits observed.

These measurements are local topology evidence only. NDCG degradation was not
measured because released human qrels are unavailable, and no production
availability or scalability claim follows from this snapshot.
