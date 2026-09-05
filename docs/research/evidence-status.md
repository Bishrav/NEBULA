# NEBULA evidence status

Assessed against commit `0c33b08`.

## Reproducible now

- The provisional research corpus contains 843 documents: 595 Kubernetes and
  248 PostgreSQL documents.
- All 843 raw and 843 normalized checksums validate with
  `tools/validate_research_corpus.py`.
- The generated corpus report records the source-level licenses as CC-BY-4.0
  and PostgreSQL License, with attribution to The Kubernetes Authors and the
  PostgreSQL Global Development Group.
- The licensing status remains `NEEDS_HUMAN_LICENSING_REVIEW`; source-level
  declarations are not legal clearance.
- The committed candidate pool contains 400 queries, all marked
  `NEEDS_HUMAN_REVIEW`.
- The synthetic regression benchmark is reproducible with
  `python tools/reproduce_benchmark.py` and has a checksum lock.

## Not present or not valid for research claims

- The protected 379-query source and 3,790 retrieval-candidate pairs are not
  present in this checkout, so their existence cannot be independently
  reproduced here.
- No human relevance labels, agreement statistic, adjudication result, or
  released qrels are present.
- Metrics from the four-document synthetic fixture must not be reported as
  metrics for the 843-document research corpus.

## Required external evidence

1. A human reviewer must confirm the license, notices, attribution, and
   redistribution status for the admitted corpus.
2. Two independent annotators must complete the protected blinded packages.
3. The returned packages must pass submission validation, agreement analysis,
   adjudication, and the final-qrels release gate.
4. Only then may the held-out research evaluation be generated and reported.
