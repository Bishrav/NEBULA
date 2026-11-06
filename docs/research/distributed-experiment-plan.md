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

Targeted fault overrides are versioned under `infrastructure/docker/` for the
latency, HTTP-500, and intermittent scenarios. Apply one with the base Compose
file, run the observation, then restore the base service configuration before
the next scenario.

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

A fresh local Docker run on 2026-11-02 used the six-node topology, Java 17
containers, the default 500 ms shard timeout, two attempts, a limit of five,
and the query "shard failure". D0 healthy completed five repetitions with no
partial responses, retries, or shard failures; p50 latency was 53.7 ms and p95
latency was 68.2 ms.

D1 stopped one primary for three repetitions. It produced partial-result
frequency 1.0, p50 latency 1014.5 ms, p95 latency 1027.3 ms, and top-k overlap
0.4 against the healthy baseline. D3 stopped both endpoints in one pair and
produced partial-result frequency 1.0, eight retries, two open-circuit
observations, p95 latency 8013.0 ms, and top-k overlap 0.4. After services were
restored, D8 completed three repetitions with no partial responses; p50 latency
was 52.2 ms and p95 latency was 67.5 ms.

These are local topology and hardware observations, not production availability
or scalability evidence. NDCG degradation was not measured because
corpus-wide relevance qrels are unavailable; the protected four-document
known-item packet is not sufficient for a corpus-wide quality-degradation claim.
The remaining D2, D4, D5, D6, and D7 scenarios
remain part of the versioned experiment matrix and should be run before making
broader resilience statements.
