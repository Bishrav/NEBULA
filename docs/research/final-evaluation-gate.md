# Final held-out evaluation gate

Phase 9 must stop unless every prerequisite passes. Run:

    python tools/check_final_evaluation_gate.py --output reports/generated/final-evaluation-gate.json

The gate checks:

- admitted, non-provisional research corpus;
- human-approved query set;
- released qrels backed by independent annotation, agreement, and adjudication;
- immutable, complete, and disjoint development/validation/test split;
- final ranking configuration;
- final embedding model;
- frozen statistical analysis protocol;
- clean Git working tree.

The evaluation runner separately requires the explicit --final-heldout-evaluation option for test/held-out query files. That option is necessary but not sufficient: it does not override missing labels, corpus licensing, split, or model-freeze requirements.

The qrels file must have a sibling `<qrels-file>.release.json` sidecar declaring
`status=release-ready`, at least two annotators, computed agreement, and completed
adjudication. This keeps a copied or manually edited qrels file from being treated
as independently validated human evidence.

## Current gate status

**GATE PASSED — PROTECTED RUN MEASURED; PUBLICATION CLAIM STILL BLOCKED**

The protected review batch has approved queries, independent annotations,
agreement, consensus adjudication, frozen splits, and released qrels. The final
ranking and embedding manifests were frozen from validation-only evidence. The
protected-path gate passed and the held-out run completed once on 8 queries.
Those artifacts remain outside the public repository. The result is measured
preliminary evidence, not a claim of statistical significance, generalization,
or user benefit.

The qrels cover only four packet documents per query: one grounding document and
three seeded random same-project distractors. The candidate builder does not
run system retrieval, and documents outside those packets are unjudged. The
gate therefore verifies protocol and artifact integrity; it does not establish
exhaustive corpus-wide relevance labels or validate the relevance rubric.

Synthetic regression evaluation remains permitted for software regression only. It must not be reported as final research evidence.
