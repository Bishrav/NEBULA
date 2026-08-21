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
