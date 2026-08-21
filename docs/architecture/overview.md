# NEBULA Architecture Overview

## Phase 0 architecture

```text
Markdown/PDF files
        |
        v
Ingestion pipeline --> document metadata and raw content
        |
        v
Document processor --> normalized fields and tokens
        |
        v
Lexical index --> BM25 query engine --> Search API
                                      |
                                      v
                             ranking explanation
```

## Planned architecture evolution

The system will evolve toward:

```text
Connectors -> Ingestion -> Processing -> Index Builder
                                      |-> Inverted Index
                                      |-> Link Graph
                                      |-> Vector Index
                                      v
                               Distributed Shards
                                      v
                              Query Coordinator
                                      v
                             Hybrid Ranking Service
                                      v
                              Search and Answer APIs
```

## Design constraints

- The core inverted index, ranking, sharding, and ANN components are implemented inside NEBULA.
- PostgreSQL stores metadata, not the primary search index.
- Search quality is evaluated against fixed, versioned queries.
- AI-generated answers are optional and cannot replace deterministic retrieval.
- Index snapshots must be reproducible and restorable.

## Distributed search baseline

`ConsistentHashRing` provides deterministic virtual-node placement for document keys. `QueryCoordinator` fans requests out to `SearchShard` instances, merges local results with deterministic tie-breaking, and reports unavailable shards through `CoordinatedSearchResponse.isPartial()`. This is an in-process research baseline; network transport, replication, retries, and durable shard membership remain production work.

`ReplicatedSearchShard` synchronously writes a primary and replica, while `ResilientQueryCoordinator` retries the replica after primary failure and records recovered or unrecoverable shard groups. The baseline does not yet include asynchronous replication, quorum writes, or network-level retry budgets.

`HttpShardClient` and `NetworkQueryCoordinator` exercise the same contract over HTTP with connect/read timeouts and bounded retries. Failed endpoints are returned as partial-result metadata; production deployments still need service discovery, authentication, circuit breaking, and observability around retry exhaustion.

Set `NEBULA_API_TOKEN` (or `-Dnebula.apiToken`) to require `Authorization: Bearer ...` on shard search requests. `ShardEndpointRegistry` parses static startup configuration in the form `shard-a=http://host-a:8082,shard-b=http://host-b:8082`. Protect tokens with the deployment secret manager; never commit them.

`HttpShardClient` records attempts, successes, failures, and circuit state through `ShardHealth`. After the configured failure threshold it opens the circuit; after cooldown it permits one half-open probe. `ShardHealthMonitor` exposes snapshots for operational dashboards. Retry budgets and cooldowns must be tuned from production latency/error data.
