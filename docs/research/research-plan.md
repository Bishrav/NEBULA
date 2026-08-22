# NEBULA Research Plan

## Working title

Trust-Oriented Hybrid Retrieval for Explainable and Fresh Engineering Knowledge Search

## Research problem

Conventional search ranks documents primarily by lexical or semantic relevance. Engineering teams also need to know whether a result is current, supported by a documented source, verifiable, and consistent with related documentation. NEBULA therefore studies a configurable set of observable signals; it does not estimate truth.

## Research question

How do observable freshness, source-authority, graph, and evidence-related signals affect engineering knowledge retrieval compared with lexical-only, semantic-only, and conventional hybrid baselines?

## Hypotheses

- RQ1: How does trust-oriented hybrid retrieval compare with lexical, semantic, and conventional hybrid baselines on held-out engineering queries?
- RQ2: Which observable signals contribute positively or negatively, and for which query categories?
- RQ3: Is performance robust to modest changes in the ranking weights, or does it depend on narrow tuning?
- RQ4: What recall-latency trade-off does HNSW exhibit as vector scale and search parameters increase?
- RQ5: How does distributed retrieval degrade under controlled shard and replica failures?

## Experimental principles

- Version the corpus, query set, labels, code, and configuration.
- Compare against simple baselines before adding model complexity.
- Report ablations for every trust-related signal and keep relevance, reliability proxies, freshness, graph importance, and evidence quality conceptually separate.
- Separate retrieval quality from generated-answer quality.
- Include latency, memory, and index-size measurements.
- Record failure cases, not only average scores.

## Current status

This is a staged research plan. The current four-document synthetic fixture is a regression benchmark, not research evidence. No superiority, user-benefit, trust, scalability, or statistical claim should be made until the relevant dataset, baselines, experiments, and uncertainty analysis are complete.

See [the formal ranking model](ranking-model.md), [the repository audit](repository-audit.md), [the readiness dashboard](research-readiness.md), and [the claim ledger](claim-ledger.md).
