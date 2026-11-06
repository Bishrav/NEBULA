# NEBULA: university review brief

**Prepared:** 2026-11-06
**Project status:** Research prototype with preliminary protected evaluation evidence

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
- Protected research evaluation on an attributed 843-document
  Kubernetes/PostgreSQL snapshot, 47 approved queries, independently created
  relevance judgements, and a frozen 30/9/8 development/validation/held-out
  split. The current held-out subset contains 8 queries and is intentionally
  reported as preliminary and underpowered.
- Dedicated six-node Docker research topology with measured healthy, primary
  failure, full-pair failure, and recovery scenarios.
- Research safeguards: frozen inputs, SHA-256 verification, development/held-out
  separation, claim ledger, annotation workflow, and explicit evidence limits.

## Current evidence boundary

The public repository contains a four-document synthetic regression fixture. It
is useful for detecting implementation changes and demonstrating the
measurement pipeline, but it is not evidence of general engineering-search
quality, statistical superiority, user benefit, production availability, or
population-level trust.

A protected evaluation package has since completed corpus licensing review with
attribution, independent annotation, agreement analysis, adjudication, and a
frozen split. One held-out run is measured on 8 queries: hybrid NDCG@10 is
0.8647 versus 0.7397 for BM25, a mean paired improvement of 0.125. Because the
test set is small, the result is descriptive preliminary evidence rather than a
publication-grade generalisation claim. Protected qrels and participant-related
materials are intentionally excluded from the public repository.

## Suggested five-minute review path

1. Read the root README for the architecture and quick start.
2. Follow the demo workflow and inspect the local search workspace.
3. Read the academic research brief for hypotheses, scope, limitations, and the
   planned study.
4. Run `tools/start_local_demo.ps1` or `python tools/reproduce_benchmark.py` to
   launch the demo or regenerate the public fixture reports.
5. Inspect the claim ledger and research-readiness dashboard to see what is and
   is not yet supported.
6. Request the protected evaluation summary separately if a supervisor wants to
   review the human-qrels protocol and held-out results.

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

**Status:** research prototype / preliminary systems research with protected
held-out evidence; broader validation and publication-grade inference remain
future work.
