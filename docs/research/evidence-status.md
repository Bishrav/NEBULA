# NEBULA evidence status

Assessed against commit `398d2f1`.

## Reproducible now

- The provisional research corpus contains 843 documents: 595 Kubernetes and
  248 PostgreSQL documents.
- All 843 raw and 843 normalized checksums validate with
  `tools/validate_research_corpus.py`.
- The generated corpus report records the source-level licenses as CC-BY-4.0
  and PostgreSQL License, with attribution to The Kubernetes Authors and the
  PostgreSQL Global Development Group. The recorded review decision is
  `APPROVED_WITH_ATTRIBUTION`; this is not legal advice or a substitute for
  institutional review.
- The protected review batch contains 47 approved queries and a frozen
  development/validation/test split (30/9/8).
- Two independent protected canonical annotation exports validate against the
  approved query source and the 843-document corpus. They contain 188 rows
  each, have zero disagreements, and report Cohen's, linear weighted, and
  quadratic weighted kappa of 1.0.
- The protected consensus release and final qrels regenerate byte-identically
  to the recorded qrels checksum. These artifacts remain outside Git because
  they contain protected research data.
- The synthetic regression benchmark is reproducible with
  `python tools/reproduce_benchmark.py` and has a checksum lock.

## Not present or not valid for research claims

- The protected annotation source, canonical labels, agreement, adjudication,
  split, and qrels are not public repository artifacts and cannot be verified
  from a fresh public clone alone.
- Final configuration promotion and a clean-tree final evaluation gate have
  not yet been completed.
- Metrics from the four-document synthetic fixture must not be reported as
  metrics for the 843-document research corpus.

## Required external evidence

1. Preserve the protected artifacts and their checksums outside the public
   repository.
2. Freeze the final ranking and embedding configuration after validation-set
   review.
3. Run the final held-out evaluation only after the clean-tree gate passes.
4. Obtain supervisor/institutional review before making human-study claims.
