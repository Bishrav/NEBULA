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

The protected review batch now has approved queries, independent annotations,
agreement, consensus adjudication, frozen splits, and released qrels. Those
artifacts remain outside the public repository. The final ranking and embedding
manifests are now frozen from validation-only evidence. The gate remains blocked
until the protected paths are supplied to the gate and the evaluation is run
from a clean, versioned revision. No final held-out claim may be made before
that run.

Synthetic regression evaluation remains permitted for software regression only. It must not be reported as final research evidence.
