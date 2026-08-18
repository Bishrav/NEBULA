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
- Exact quoted phrase queries using positional postings
- Search catalog that joins ingestion and lexical retrieval
- HTTP API for indexing Markdown and querying ranked results
- Frequency-ranked prefix autocomplete with a trie
- Delta and variable-byte posting-list compression
- Versioned immutable index segments with atomic snapshot writes

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

Quoted phrases use positional postings instead of a string contains check:

```text
GET /v1/search?q=%22query%20coordinator%22
```

Autocomplete is available through:

```text
GET /v1/suggest?q=serv&limit=10
```

Suggestions are ranked by observed term frequency and then by a stable lexical tie-break.

## Compression

Posting positions and document ordinals use delta encoding followed by variable-byte encoding. The codec has a correctness round-trip test and records the compact representation before segment persistence is introduced.

## Immutable segments

`IndexSegmentWriter` persists document metadata, terms, postings, and compressed positions in a versioned binary segment. It writes to a temporary file and atomically replaces the target when the platform supports atomic moves. `IndexSegmentReader` validates the segment header and reconstructs a read-only snapshot.
