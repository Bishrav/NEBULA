# ADR-0001: Initial Product Scope

## Status

Accepted

## Decision

NEBULA will begin as a trustworthy engineering knowledge search product using Markdown and PDF ingestion, explainable BM25 search, source evidence, freshness metadata, and measurable evaluation.

## Context

The original specification contains a complete distributed search-engine roadmap. Building every component before validating user value creates unnecessary product risk. A narrow lexical MVP gives us a useful baseline and creates the evidence needed to justify semantic and distributed features.

## Consequences

Positive:

- Product value can be tested early.
- BM25 becomes a measurable baseline for later research.
- The project can grow incrementally into hybrid and distributed retrieval.
- Ranking explanations and evidence support the trust-focused product thesis.

Trade-offs:

- The first version will not demonstrate the full distributed architecture.
- Connector coverage will initially be limited.
- AI answers will not be the first feature.
