# Human query review batch 01

This batch is a deterministic review sample. It contains no relevance judgements and no approved queries.

Review each query for realism, clarity, category, difficulty, grounding, freshness sensitivity, authority sensitivity, and evidence requirement.
Use APPROVE, REWRITE, or REJECT in the reviewer_decision column. Record the reason in reviewer_notes.

## Distribution

| Dimension | Values |
| --- | --- |
| Total | 60 |
| Projects | Kubernetes: 30, PostgreSQL: 30 |
| Categories | API usage: 4, architecture: 5, compatibility: 6, configuration: 5, database administration: 4, debugging: 4, deployment: 4, failure recovery: 4, migration: 4, networking: 5, observability: 5, performance: 4, security: 6 |
| Difficulty | advanced: 14, intermediate: 46 |

## Review rule

Do not assign document relevance labels in this batch. Approval means only that the information need is suitable for a later retrieval-judgement study.
