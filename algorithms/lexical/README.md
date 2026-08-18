# NEBULA Lexical Index

The lexical index is the first search-engine component in NEBULA.

## Current capabilities

- Locale-stable lowercase tokenization
- Unicode letter and number token support
- Positional posting lists
- Term frequency and document frequency
- Document length and average document length statistics
- Duplicate document protection
- Deterministic in-memory indexing
- BM25 ranking with configurable `k1` and `b`
- Stable top-k ordering
- Per-term score contributions for explainability
- Search catalog that joins ingestion and lexical retrieval
- HTTP API for indexing Markdown and querying ranked results

The initial analyzer intentionally does not remove stop words or stem terms. Those policies will be evaluated against a labelled query set rather than introduced without evidence.

## BM25 baseline

The current ranker uses:

```text
IDF(t) = log(1 + (N - df(t) + 0.5) / (df(t) + 0.5))

score(D, Q) = sum(IDF(t) * TF_normalized(t, D))
```

The default parameters are `k1 = 1.2` and `b = 0.75`. Each result exposes the contribution of every matched query term so later freshness, authority, and semantic signals can be evaluated separately.

## Search API

The local lexical service runs on port `8082`:

```powershell
java -cp .\algorithms\lexical\out com.nebula.search.LexicalSearchHttpServer
```

Index a document:

```powershell
Invoke-WebRequest `
  -Method Post `
  -Uri http://127.0.0.1:8082/v1/index/documents `
  -Headers @{ 'X-Source-Path' = 'docs/runbook.md' } `
  -ContentType 'text/markdown' `
  -Body '# Runbook`n`nCheck service health.'
```

Search the index:

```text
GET /v1/search?q=service%20health&limit=10
```

Every result includes the document ID, title, source path, BM25 score, and per-term score contributions.
