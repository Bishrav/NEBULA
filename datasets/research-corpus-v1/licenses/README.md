# License evidence and attribution

This directory is intentionally a review surface. The source registry records the official evidence URLs and the corpus report records the declared license and attribution per document.

Current source-level evidence:

- Kubernetes website: [official repository license](https://github.com/kubernetes/website/blob/main/LICENSE), CC BY 4.0.
- PostgreSQL: [official license page](https://www.postgresql.org/about/licence/) and upstream `COPYRIGHT` notice.

Status: **NEEDS HUMAN LICENSING REVIEW** for third-party excerpts, generated content, attribution, and redistribution obligations.
Retained pinned-commit notices:

- source-kubernetes-docs-LICENSE.txt from Kubernetes website commit 9b5eeefac89219bc2b1d6875c0d5bd7071e57ab6.
- source-postgresql-docs-COPYRIGHT.txt from PostgreSQL commit e2c812f1475dc75ab6f8a39fb5696d8d32d05fa1.

Run 	ools/audit_corpus_licensing.py to verify source-level evidence and retained notices. The audit reports structural readiness; it deliberately leaves legal clearance as NOT_DECIDED until human review.
