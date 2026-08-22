# Data-management plan

**DRAFT — REQUIRES INSTITUTIONAL/SUPERVISOR REVIEW**

## Data categories

| Category | Examples | Planned handling |
| --- | --- | --- |
| Session telemetry | session code, query, result count, latency, task event | restricted raw storage |
| Study responses | confidence, questionnaire answers, optional comments | restricted raw storage |
| Consent/contact record | consent outcome, contact route if needed | separate restricted store |
| Derived analysis | aggregate task metrics, anonymized summaries | versioned research output |
| Public release | synthetic or explicitly consented aggregate data | release only after review |

## Collection boundary

The study application must use a fresh telemetry file and a consent gate. It must not collect names, email addresses, account identifiers, unrelated browsing data, passwords, private documents, or workplace-confidential content. Observers must record notes outside telemetry and remove identifying details before analysis.

## Pseudonymization and separation

Use a randomly generated study code. Keep any code-to-contact mapping, signed consent record, or recruitment list in a separate access-controlled location. Never place that mapping in Git, experiment artifacts, or the telemetry directory.

## Access

Approved access list: __________________  
Storage location/jurisdiction: __________________  
Encryption/access-control mechanism: __________________  
Data controller/owner: __________________

Raw data access is limited to the approved research team. CI, public GitHub, demo deployments, and external analytics must not receive raw participant data.

## Quality and audit

Preserve the original raw export read-only. Record checksums, corpus/query/code versions, exclusions, redaction decisions, and derived-file lineage. Never overwrite original responses during cleaning or adjudication.

## Retention and deletion

Raw retention period: __________________  
Raw deletion date: __________________  
Derived-data retention: __________________  
Deletion owner and verification record: __________________

Withdrawal requests must be logged separately. If data has already been irreversibly aggregated, explain that limitation in the approved consent form.

## Release

No human-level raw export is currently approved for release. Any public derivative must pass privacy review, remove free-text identifiers, preserve only the minimum necessary fields, and include a provenance manifest.
