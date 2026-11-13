# Fresh sealed held-out evaluation plan

**Status:** BLOCKED ON FRESH QUERY AUTHORING AND HUMAN ANNOTATION

## Why a new test is required

The original 8-query held-out run is already spent and is too small for a
publication-grade conclusion. The current 39-query pooled wave is valid
development/validation evidence, but it must not be reused as a new held-out
test after configuration review. The public checkout contains query text, but
it does not contain a fresh sealed test set, protected labels, or the pinned
BGE cache for new query text.

## Completion protocol

1. Author and approve a fresh test set of at least 12 queries, balanced across
   Kubernetes/PostgreSQL, query categories, difficulty, and freshness
   sensitivity. The author must not use results from the future test run.
2. Seal the query file, split manifest, corpus manifest, ranking configuration,
   pinned BGE model ID, fixed evaluation timestamp, and statistical analysis
   plan. Record SHA-256 checksums before retrieval or annotation.
3. Generate a new offline embedding cache covering every corpus document and
   every fresh query. Require the exact configured model
   `BAAI/bge-base-en-v1.5@a5beb1e3e68b9ab74eb54cfd186867f64f240e1a` and record
   the cache and embedding-config checksums.
4. Build a depth-10 union pool from BM25, semantic, hybrid, BGE-hybrid, and
   A3-A9. Do not inject grounding documents. Record per-query pool size,
   provenance, and output checksums.
5. Create two blinded annotation packages. Reviewers must label independently
   without retrieval metadata or access to one another's labels.
6. Validate completeness and agreement over the full pool, adjudicate
   disagreements, and release qrels only with the required release sidecar.
7. Freeze the final test inputs and run each registered system exactly once.
   Report per-query metrics, paired differences, uncertainty intervals,
   multiplicity correction, judged-positive counts, and unjudged-document
   limitations.
8. Update the claim ledger, readiness dashboard, research summary, and outreach
   brief only after the run and checksums are independently reviewed.

## Current stopping point

Repository-side guards and tooling are ready. Work must stop before claiming
completion until the fresh query file, protected split, new cache, and human
annotations exist. The existing 39-query pooled results remain preliminary
development/validation evidence; the old 8-query comparison remains separate
descriptive known-item evidence.
