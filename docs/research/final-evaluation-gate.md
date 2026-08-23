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

**BLOCKED — NOT READY FOR FINAL HELD-OUT EVALUATION**

The current repository has a provisional corpus, candidate queries marked NEEDS_HUMAN_REVIEW, no released human qrels, no frozen research split, and no final ranking/embedding configuration. No final held-out metrics may be generated from this state.

Synthetic regression evaluation remains permitted for software regression only. It must not be reported as final research evidence.
