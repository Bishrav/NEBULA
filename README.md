# NEBULA

## Trustworthy, explainable hybrid search for engineering knowledge

NEBULA is a research-driven, privacy-first search and retrieval platform for engineering teams. It is designed to help developers find technical knowledge, verify the evidence behind a result, and identify information that is stale, duplicated, or contradictory.

NEBULA is also a search-engine laboratory. Its core indexing, ranking, compression, sharding, and approximate-nearest-neighbour components are implemented from first principles so that relevance, latency, memory, and reliability can be measured rather than hidden behind a black-box search product.

> **Project status:** Phase 0 - product discovery and foundation

## Why NEBULA exists

Engineering knowledge is distributed across READMEs, architecture decisions, runbooks, incident reports, API specifications, and project notes. Existing keyword search misses meaning, while AI search can return plausible answers without enough evidence or freshness context.

NEBULA explores a different approach:

> Combine lexical precision, semantic similarity, document relationships, freshness, authority, and evidence into a transparent retrieval system.

## Product vision

NEBULA will become a self-hostable knowledge search system for small and medium-sized engineering teams. A user should be able to ask a technical question and receive:

- Relevant documents and passages
- A clear explanation of ranking signals
- Source and ownership information
- Freshness and verification status
- Related and conflicting documents
- An optional answer grounded in cited evidence

## Initial product slice

The first implementation deliberately starts with a measurable lexical baseline:

1. Ingest Markdown and PDF engineering documents.
2. Normalize, version, and deduplicate documents.
3. Build an inverted index from first principles.
4. Rank results with TF-IDF and BM25.
5. Support field-aware and phrase queries.
6. Display matching passages and ranking explanations.
7. Track freshness and source metadata.
8. Evaluate results against a versioned labelled query set.

Semantic retrieval, distributed shards, and grounded answers will be added after the lexical baseline is measurable.

## Planned capabilities

| Area | Planned capability |
| --- | --- |
| Ingestion | Markdown, PDF, repository, and documentation connectors |
| Indexing | Inverted index, forward index, immutable segments, background merges |
| Ranking | TF-IDF, BM25, field boosts, freshness, authority, and hybrid fusion |
| Search UX | Phrase queries, filters, autocomplete, explanations, and evidence passages |
| Trust | Freshness, verification, ownership, duplicate, and contradiction signals |
| Graph intelligence | Link extraction, related documents, and PageRank |
| Semantic retrieval | Embeddings, exact cosine baseline, and custom HNSW |
| Distributed systems | Sharding, consistent hashing, query coordination, replicas, and failure handling |
| Research | Reproducible datasets, baselines, ablations, benchmarks, and error analysis |

## System design

```text
                         +-----------------------+
                         |  Web UI / API Clients |
                         +-----------+-----------+
                                     |
                         +-----------v-----------+
                         |      Search API       |
                         +-----------+-----------+
                                     |
                         +-----------v-----------+
                         | Query Coordinator     |
                         | fan-out / timeout     |
                         | global top-k merge    |
                         +-----+-----------+-----+
                               |           |
                    +----------v--+   +---v-----------+
                    | Shard Server |   | Trust / Hybrid|
                    | BM25 index   |   | Ranking       |
                    | Vector index |   | explanations  |
                    +------+-------+   +-------+-------+
                           |                     |
              +------------v---------------------v-----------+
              | Inverted Index | Vector Index | Link Graph   |
              +----------------+--------------+--------------+
                                     ^
                                     |
              +---------------------+-----------------------+
              | Document Processing and Index Builder       |
              | parse / normalize / deduplicate / tokenize  |
              | extract links / generate embeddings         |
              +---------------------^-----------------------+
                                    |
              +---------------------+-----------------------+
              | Connectors: Markdown, PDF, repositories    |
              +---------------------------------------------+
```

### Core data flow

```text
Source document
  -> ingestion job
  -> canonical document and version
  -> normalized fields and tokens
  -> immutable index segment
  -> segment merge and snapshot
  -> shard query
  -> hybrid ranking and evidence response
```

### Planned infrastructure

- Java for indexing, ranking, shard, and algorithm services
- Python for embeddings, evaluation, and model experiments
- PostgreSQL for metadata and job state
- Redis for cache and coordination where useful
- Kafka or Redpanda for versioned asynchronous events
- MinIO or S3-compatible storage for raw documents and index snapshots
- Prometheus, Grafana, and OpenTelemetry for observability
- Docker Compose for local development, Kubernetes after the core is stable

## Research direction

The planned research question is:

> Does trust-aware hybrid ranking improve the relevance, verifiability, and freshness of engineering knowledge retrieval compared with lexical-only, semantic-only, and conventional hybrid baselines?

The research will compare:

- TF-IDF
- BM25
- Dense vector retrieval
- BM25 plus vector retrieval
- Hybrid retrieval with trust signals
- Exact vector search versus custom HNSW

Evaluation will include Precision@k, Recall@k, MRR, NDCG, citation accuracy, stale-result rate, p50/p95 latency, index size, memory usage, throughput, and failure recovery measurements.

## Repository layout

```text
apps/             User-facing applications and API gateway
services/         Ingestion, indexing, query, and ranking services
algorithms/       Search and data-structure implementations
ml/               Embeddings, evaluation, and model experiments
schemas/          Versioned events and API schemas
tests/            Unit, integration, contract, performance, and E2E tests
benchmarks/       Reproducible performance experiments
infrastructure/   Docker, deployment, and observability configuration
docs/             Product, architecture, ADRs, and operations documentation
```

## Engineering standards

- Build the search core from first principles; do not hide the core behind Elasticsearch.
- Keep deterministic retrieval and ranking separate from generative AI.
- Establish a measurable baseline before adding complexity.
- Every major design choice gets an Architecture Decision Record.
- Every performance or quality claim must have a reproducible benchmark.
- Every service exposes health, readiness, and metrics endpoints.
- Critical workflows use unit, integration, contract, performance, and end-to-end tests.
- AI outputs must be schema-validated and grounded in retrieved evidence.

## Development roadmap

1. Product discovery, evaluation corpus, and repository foundation
2. Document ingestion and metadata lifecycle
3. Inverted index, TF-IDF, BM25, and phrase queries
4. Posting compression and autocomplete
5. Trust signals, freshness, evidence, and quality feedback
6. Link graph and PageRank
7. Sharding, consistent hashing, and query coordination
8. Semantic retrieval and custom HNSW
9. Hybrid ranking, grounded answers, and pilot deployment
10. Research experiments, technical report, and reproducible release

## Documentation map

- [Product brief](docs/product/product-brief.md)
- [Phase 0 validation plan](docs/product/validation-plan.md)
- [System architecture](docs/architecture/overview.md)
- [Research plan](docs/research/research-plan.md)
- [Evaluation metrics](docs/evaluation/metrics.md)
- [ADR-0001: Initial product scope](docs/adr/0001-initial-product-scope.md)

## Responsible use and data

Do not commit private company documents, credentials, production indexes, API keys, or personal data. Use synthetic, public, or explicitly consented documents for examples and evaluation. Permission-aware retrieval is a product requirement before connecting private sources.

## License

License selection will be made before the first public implementation release.

## Product thesis

Engineering teams do not only need relevant search results; they need results they can verify and trust. NEBULA treats evidence, freshness, authority, and explainability as first-class retrieval signals.

## Engineering principles

- Build the search core from first principles; do not hide the core behind Elasticsearch.
- Keep deterministic ranking and retrieval logic separate from generative AI.
- Establish a measurable baseline before adding complexity.
- Every major design choice gets an architecture decision record.
- Every performance or quality claim must have a reproducible benchmark.
- All services expose health, readiness, and metrics endpoints.
