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
- A fresh protected pooled wave now contains 16 sealed test queries and 308
  graded judgments over 843 public-corpus documents. The evaluator ranks the
  full corpus, while only the pooled documents are judged; this is a narrow
  pooled known-item/source-page benchmark, not exhaustive corpus-wide qrels.
- The current frozen run produced BM25 NDCG@5 `0.7165`, conventional hybrid
  `0.8703`, and pinned BGE-hybrid `0.8544`. The best A3-A9 variants reached
  `0.8738`; A5, A7, and A8 reached `0.8567`, while A9 reached `0.8345`.
  These are descriptive results from 16 queries, not a general superiority or
  production claim.
- The paired annotation agreement was 97.08% with Cohen's kappa `0.9163`;
  all 9 disagreements were adjudicated before qrels release. The trust
  variants now use the reviewed authority metadata and a derived graph with
  1,096 resolved internal edges. Authority remains uniform across the corpus,
  so the results do not isolate causal authority effects; pooled coverage also
  remains incomplete.

- The pooled evaluation uses the pinned 768-dimensional BGE cache, an
  explicit fixed timestamp, and checksum-recorded trust and graph inputs.
- The current corpus still has one authority category and uniform authority
  metadata. The manifest's original graph-link field is empty, while the
  separately derived graph used for the fresh run contains 1,096 resolved
  internal edges. A3-A9 therefore remain comparative variant results, not
  isolated causal signal ablations.

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
