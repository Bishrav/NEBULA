# NEBULA Product Brief

## Product

NEBULA is a self-hostable engineering knowledge search platform. It helps software teams find, verify, and maintain technical knowledge spread across documentation, runbooks, architecture records, repositories, and incident notes.

## Initial customer

Small and medium-sized engineering teams with approximately 10–100 engineers and fragmented internal documentation. The first deployment should work for a single team or workspace without requiring a large platform team.

## User problem

Engineers lose time searching across disconnected systems and often cannot tell whether a result is authoritative, current, or contradicted by another document.

## Initial promise

Find a useful engineering answer quickly, inspect the evidence behind it, and see whether the source is current and trustworthy.

## MVP scope

- Markdown and PDF ingestion
- Document metadata and versioning
- Duplicate detection
- Inverted index
- TF-IDF baseline
- BM25 ranking
- Phrase queries
- Title, heading, and body boosts
- Search filters
- Matching passages and ranking explanations
- Freshness indicators
- Labelled evaluation dataset and metrics

## Explicitly out of scope for the MVP

- Many third-party connectors
- Multi-region deployment
- Autonomous agents
- Complex reranking models
- Production-scale web crawling
- Kubernetes-first deployment

## Product risks

1. Users may see NEBULA as another generic search interface.
2. The first document connectors may not represent real workflows.
3. Search quality may be difficult to evaluate without labelled queries.
4. Trust signals may look useful but fail to improve decisions.
5. Building infrastructure may consume time before user value is proven.

## Product advantage hypothesis

Evidence, freshness, authority, and explainable ranking can make engineering search more useful and trusted than relevance-only or citation-free AI search.
