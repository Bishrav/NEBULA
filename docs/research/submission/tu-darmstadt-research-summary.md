# NEBULA — research summary for academic review

**Prepared:** 2026-11-09  
**Author:** Bishrav Shiwakoti  
**Status:** Research prototype with preliminary, protected evaluation evidence

## Project in one paragraph

NEBULA is an inspectable Java 17 research prototype for engineering-knowledge
retrieval. It compares BM25, semantic retrieval, hybrid fusion, BGE-backed
retrieval, graph signals, freshness, source-authority metadata, and
failure-aware distributed search. The intended contribution is a reproducible
experimental framework for studying observable ranking signals—not a claim to
invent BM25, dense retrieval, HNSW, PageRank, or distributed systems methods.

## Research question

How do observable freshness, source-authority, graph, and evidence-related
signals affect engineering-knowledge retrieval compared with lexical-only,
semantic-only, and conventional hybrid baselines?

## Reproducible study setup

- Public corpus: 843 documents from pinned Kubernetes and PostgreSQL sources.
- Query set: 47 approved query texts with a protected 30/9/8
  development/validation/test split.
- New pooled wave: 39 development/validation queries and 709 judged
  query-document pairs.
- Fresh sealed test wave: 16 queries and 308 pooled judgments, kept outside
  the public repository.
- Annotation: two separate blinded annotation exports, independently completed
  and validated; the fresh wave had 9 label disagreements, all adjudicated by
  Laxmi Acharya before qrels release.
- Retrieval pool: top-10 results from the pre-registered systems and a pinned
  768-dimensional BGE cache. The fresh corpus has uniform authority metadata;
  the run used a derived graph with 1,096 resolved internal edges, so the
  trust-signal contribution is still not isolated.
- Reproducibility: SHA-256 checksums, fixed timestamps, split guards, private
  artifact manifests, Java/Python tests, and a claim ledger.

## Preliminary pooled results

These are development/validation results over pooled judgments. They are not
the final held-out test and do not establish corpus-wide relevance because
unpooled documents remain unjudged.

| System | NDCG@5 |
| --- | ---: |
| BM25 | 0.7147 |
| BGE semantic | 0.8585 |
| Hybrid | 0.9047 |
| A3 — freshness | 0.9120 |
| A4 — authority | 0.9118 |
| A5 — graph | 0.9111 |
| A6 — freshness + authority | 0.9120 |
| A7 — authority + graph | 0.9113 |
| A8 — freshness + graph | 0.9113 |
| A9 — full combination | 0.9035 |

The 16-configuration weight-sensitivity grid ranges from NDCG@5 `0.5196` to
`0.9249`, showing that the ranking configuration is materially sensitive to
weight choices. The full grid, not only the best configuration, is retained in
the protected artifacts.

## Evidence boundary

The results primarily measure known-item/source-page retrieval against a pooled
judgment set. They are preliminary and should not be described as proof of
general search quality, statistical superiority, user benefit, or trust
estimation. Authority values are source-priority inputs, not truth labels.

The fresh 16-query held-out run produced BM25 NDCG@5 `0.7165`, conventional
hybrid `0.8703`, and pinned BGE-hybrid `0.8544`. The BGE-hybrid delta was
`+0.1379` NDCG@5. These remain descriptive known-item/source-page results
against incomplete pooled qrels, not publication-grade evidence. A3-A9 ranged
from `0.8345` to `0.8738`; because authority is uniform and pooled coverage is
incomplete, they do not constitute a separated causal trust-signal ablation.

## Questions for academic supervision

I would value guidance on:

1. narrowing the primary information-retrieval contribution;
2. selecting defensible estimands and baselines for the pooled evaluation;
3. handling incomplete pooled judgments and unjudged documents;
4. testing authority, freshness, and graph signals for bias and manipulation;
5. designing a fresh sealed held-out evaluation; and
6. deciding whether NEBULA could become a dissertation or research-project
   direction.

## Review path

1. Read the root README and [research-readiness dashboard](../research-readiness.md).
2. Read the [pooled annotation protocol](../pooled-annotation-evaluation.md).
3. Inspect the [claim ledger](../claim-ledger.md).
4. Run the public regression benchmark and Python test suite.
5. Request protected evaluation artifacts separately if a supervisor wants to
   inspect the annotation and checksum records.

Repository: https://github.com/Bishrav/NEBULA  
Portfolio: https://portfolio-self-phi-65.vercel.app/
