# NEBULA corpus licensing review

Status: HUMAN REVIEW REQUIRED  
Current machine audit: STRUCTURAL PASS, legal clearance NOT_DECIDED  
Audit artifact: data/review-batch-01-license-audit.json

This checklist records the human review needed before the research corpus is
described as a final public release. It is not a legal opinion.

## Corpus scope

- Corpus: research-corpus-v1
- Documents: 843
- Sources: Kubernetes documentation (595 documents) and PostgreSQL documentation (248 documents)
- Transformations: raw upstream files are retained; normalized text removes deterministic formatting artifacts and records checksums in the manifest.
- Registry: benchmarks/research/source-registry-v1.psv
- Manifest: datasets/research-corpus-v1/manifests/research-corpus-v1.json

## Source review

### Kubernetes documentation

- Declared license: CC-BY-4.0
- Attribution: The Kubernetes Authors
- Evidence: https://github.com/kubernetes/website/blob/main/LICENSE
- Retained notice: datasets/research-corpus-v1/licenses/source-kubernetes-docs-LICENSE.txt
- Scope: content/en/docs/**/*.md and Markdown-compatible documentation files
- Reviewer checks:
  - [ ] Confirm the license text matches the pinned upstream commit.
  - [ ] Confirm the retained attribution is sufficient for redistribution.
  - [ ] Check admitted paths for third-party notices, embedded assets, or generated material.
  - [ ] Confirm normalized derivatives preserve required attribution context.

### PostgreSQL documentation

- Declared license: PostgreSQL License
- Attribution: PostgreSQL Global Development Group
- Evidence: https://www.postgresql.org/about/licence/
- Retained notice: datasets/research-corpus-v1/licenses/source-postgresql-docs-COPYRIGHT.txt
- Scope: doc/src/sgml/**/*.sgml and XML-compatible documentation files
- Reviewer checks:
  - [ ] Confirm the license and COPYRIGHT text match the pinned upstream commit.
  - [ ] Confirm the retained attribution is sufficient for redistribution.
  - [ ] Check admitted paths for third-party notices, generated material, or bundled examples.
  - [ ] Confirm normalized derivatives preserve required attribution context.

## Decision record

The reviewer should record one decision per source:

| Source | Decision | Reviewer | Date | Notes |
| --- | --- | --- | --- | --- |
| Kubernetes documentation | PENDING |  |  |  |
| PostgreSQL documentation | PENDING |  |  |  |

Allowed decisions: APPROVED, APPROVED_WITH_ATTRIBUTION, BLOCKED, or PENDING.

The corpus must remain provisional while any source is PENDING or BLOCKED.
After review, update the registry notes and the corpus release metadata with the
decision and preserve the reviewer identity outside public research artifacts
when required by the study data-management plan.
