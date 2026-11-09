# NEBULA evidence status

Assessed against the current public `main` branch.

## Reproducible now

- The public research corpus contains 843 documents: 595 Kubernetes and
  248 PostgreSQL documents.
- All 843 raw and 843 normalized checksums validate with
  `tools/validate_research_corpus.py`.
- The generated corpus report records the source-level licenses as CC-BY-4.0
  and PostgreSQL License, with attribution to The Kubernetes Authors and the
  PostgreSQL Global Development Group. The recorded review decision is
  `APPROVED_WITH_ATTRIBUTION`; this is not legal advice or a substitute for
  institutional review.
- The public repository contains the 47 approved query texts; protected labels,
  adjudication records, and the frozen development/validation/test split
  (30/9/8) remain outside the public checkout.
- Two independent protected canonical annotation exports validate against the
  approved query source and the 843-document corpus. They contain 188 rows
  each, have zero disagreements, and report Cohen's, linear weighted, and
  quadratic weighted kappa of 1.0.
- The 188 rows are exactly 47 queries × 4 candidates: one query-grounding
  document plus three seeded random same-project distractors. The candidate
  builder does not run system retrieval, and the remaining corpus documents are
  unjudged.
- Independence was enforced operationally: each annotator received a separate
  blinded package, labelled without access to the other annotator's export or
  ranking-system outputs, and submitted the completed export before agreement
  analysis and adjudication.
- The protected consensus release and final qrels regenerate byte-identically
  to the recorded qrels checksum. These artifacts remain outside Git because
  they contain protected research data.
- The synthetic regression benchmark is reproducible with
  `python tools/reproduce_benchmark.py` and has a checksum lock.
- After the protected final-evaluation gate passed, one frozen held-out run was
  measured by ranking the 843-document public corpus for 8 queries. The
  protected qrels contain four labelled packet documents per query: BM25 NDCG@5
  `0.739665` versus frozen BGE hybrid NDCG@5 `0.864665`. This is preliminary
  known-item/source-page identification evidence against incomplete judgments,
  not corpus-wide graded relevance evidence or a generalization/superiority
  claim.
- The paired analysis over those 8 queries produced mean NDCG@5 difference
  `+0.125000`, median `0.000000`, bootstrap 95% interval `[0.000000,
  0.375000]`, permutation p-value `1.0`, and Holm-adjusted p-value `1.0`.
  The result is descriptive and underpowered.

- A new private pooled annotation wave covers 39 development/validation
  queries and 709 retrieved query-document pairs. Both independent exports
  validate completely, with zero disagreements; this is a new evaluation
  input, not a rerun of the spent 8-query test.
- The pooled evaluation uses the pinned 768-dimensional BGE cache, an
  explicit fixed timestamp, and a derived internal graph with 1,096 resolved
  edges. The graph is provenance-tracked and excludes unresolved or external
  links.
- The current corpus still has one authority category and uniform authority
  metadata. A4/A6/A7/A9 authority effects therefore remain diagnostic, not a
  validated authority-ablation result.

## Not present or not valid for research claims

- The protected annotation source, canonical labels, agreement, adjudication,
  split, and qrels are not public repository artifacts and cannot be verified
  from a fresh public clone alone.
- The held-out run is intentionally small and underpowered for publication-grade
  inference; supervisor review and broader independently judged evidence remain
  required.
- Metrics from the four-document synthetic fixture must not be reported as
  metrics for the 843-document research corpus.
- The protected qrels must not be described as exhaustive or corpus-wide
  relevance judgements. Unjudged documents are not established negatives, and
  the evaluator ranks the full corpus while the current NDCG/MRR values score
  only the four-document annotation packet. They therefore primarily measure
  known-item/source-page retrieval, not ranking within a four-item candidate
  set.

## Required external evidence

1. Preserve the protected artifacts and their checksums outside the public
   repository.
2. Freeze the final ranking and embedding configuration after validation-set
   review.
3. Treat the protected held-out run as preliminary until the statistical and
   study protocol support a claim.
4. Obtain supervisor/institutional review before making human-study claims.
