# NEBULA: university review brief

## One-minute overview

NEBULA is a research prototype for engineering-knowledge search. It combines
lexical retrieval, transparent semantic baselines, hybrid ranking, freshness,
source-authority, graph context, evidence presentation, and failure-aware
distributed retrieval.

The intended research contribution is an inspectable and reproducible
evaluation framework, not a claim that NEBULA invents BM25, dense retrieval,
RRF, HNSW, PageRank, replication, or circuit breaking.

## Research question

How do observable freshness, source-authority, graph, and evidence-related
signals affect engineering knowledge retrieval compared with lexical-only,
semantic-only, and conventional hybrid baselines?

The project keeps this question separate from secondary systems questions about
ANN latency/recall and distributed failure behaviour.

## What can be reviewed today

- Java 17 retrieval core with BM25, phrase search, semantic baselines, exact
  cosine search, custom HNSW, hybrid fusion, RRF, graph authority, and
  trust-oriented variants.
- Browser demo with result explanations, source evidence, trust signals, and
  explicit offline/partial-result boundaries.
- Reproducible synthetic benchmark with 30 queries, 14 variants, per-query
  metrics, paired uncertainty analysis, ablations, weight sensitivity, category
  summaries, and failure classifications.
- Dedicated six-node Docker research topology with measured healthy, primary
  failure, full-pair failure, and recovery scenarios.
- Research safeguards: frozen inputs, SHA-256 verification, development/held-out
  separation, claim ledger, annotation workflow, and explicit evidence limits.

## Current evidence boundary

The current benchmark is a four-document synthetic regression fixture. It is
useful for detecting implementation changes and demonstrating the measurement
pipeline. It is not evidence of general engineering-search quality, statistical
superiority, user benefit, production availability, or population-level trust.

The public research corpus and candidate query pool still require licensing
review, human query review, independent relevance judgements, agreement
analysis, adjudication, and a frozen final split.

## Suggested five-minute review path

1. Read the root README for the architecture and quick start.
2. Follow the demo workflow and inspect the five screenshots.
3. Read the academic research brief for hypotheses, scope, limitations, and the
   planned study.
4. Run python tools/reproduce_benchmark.py to regenerate the fixture reports.
5. Inspect the claim ledger and research-readiness dashboard to see what is and
   is not yet supported.

## Reproducibility entry points

From the repository root:

~~~powershell
python tools/reproduce_benchmark.py
python -m unittest discover tools -p "test_*.py"
python tools/validate_claim_ledger.py docs/research/claim-ledger.md
~~~

The reproducer verifies the frozen input lock and writes generated artifacts
under reports/generated/regression-v1/. Generated reports are intentionally
not treated as human-judged research evidence.

## Questions for academic supervision

The project would benefit from guidance on:

- narrowing the primary information-retrieval contribution;
- designing a defensible relevance-judgement and agreement protocol;
- selecting appropriate baselines and statistical estimands;
- evaluating metadata bias and adversarial trust-signal failure cases;
- separating retrieval quality from distributed availability claims;
- deciding which parts are suitable for a dissertation chapter or paper.

## Contact and status

This document is a project review brief, not an ethics submission or a claim of
university affiliation. Supervisor, institution, author, and contact details
should be added only by the project owner before sending it externally.

**Status:** research prototype / preliminary systems research infrastructure.
