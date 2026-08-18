# NEBULA Research Plan

## Working title

Trust-Aware Hybrid Retrieval for Explainable and Fresh Engineering Knowledge Search

## Research problem

Conventional search ranks documents primarily by lexical or semantic relevance. Engineering teams also need to know whether a result is current, authoritative, verifiable, and consistent with related documentation.

## Research question

Does adding trust signals to hybrid lexical-semantic retrieval improve retrieval quality and user verifiability compared with lexical-only, semantic-only, and conventional hybrid baselines?

## Hypotheses

- H1: Hybrid lexical-semantic retrieval improves MRR and NDCG over BM25-only retrieval on natural-language engineering queries.
- H2: Freshness and source-authority signals reduce the stale-result rate.
- H3: Evidence passages and ranking explanations improve user confidence and verification speed.
- H4: Custom HNSW reduces latency while maintaining an acceptable recall@k compared with exact vector search.

## Experimental principles

- Version the corpus, query set, labels, code, and configuration.
- Compare against simple baselines before adding model complexity.
- Report ablations for every trust signal.
- Separate retrieval quality from generated-answer quality.
- Include latency, memory, and index-size measurements.
- Record failure cases, not only average scores.

## Current status

This is a future research plan. No research claim should be made until the dataset, baselines, experiments, and results are complete.
