# NEBULA supervised pilot session packet

This packet is an operational aid for an approved exploratory pilot. It is not a substitute for institutional ethics review, an approved consent form, or local data-protection requirements. Do not recruit or collect participant data until those requirements are satisfied.

## Session header

Complete before the participant arrives. Use a study code, never a name or email.

| Field | Value |
| --- | --- |
| Study wave | `wave-01` |
| Session code | __________________ |
| Study version | __________________ |
| Corpus version | __________________ |
| Query-set version | __________________ |
| NEBULA commit | __________________ |
| Preflight result path | __________________ |
| Observer | __________________ |

## Participant-facing introduction

Read the approved consent summary verbatim. Explain that the session evaluates the search experience, not the participant. Explain what telemetry is collected, that participation is voluntary, that the participant may stop at any time, and how data will be protected and retained. Answer questions before opening the application.

Do not improvise consent language. Record only the approved consent outcome in the study records; do not put names, contact details, or free-form identifying information into NEBULA telemetry.

## Neutral task script

Give one task at a time. Do not suggest query words, ranking modes, documents, or the expected answer. If the participant asks whether an action is allowed, say: “Use the interface in the way you normally would when investigating this problem.”

1. **Deployment rollback:** “You need to decide how to safely roll back a deployment. Find and verify the source you would use.”
2. **Shard failure:** “A shard has failed in production. Find and verify the operational source you would use to respond.”
3. **Retrieval explanation:** “You are evaluating a retrieval change. Find and verify evidence that explains hybrid retrieval.”

For each task, start the task control before reading the prompt aloud. Stop the task when the participant identifies a source and explains why it answers the need, or when the five-minute limit is reached. Use the agreed rubric to mark success; do not infer success from a click alone.

After each completed task, ask:

> “On a scale from 1 to 5, how confident are you that the source you selected supports your answer? What made you trust or reject it?”

Do not rephrase the question to steer the answer. Leave the explanation optional and redact sensitive operational details before sharing.

## Observer record

| Task | Start time | End time | Success | First useful source | Reformulations | Confidence | Notes |
| --- | --- | --- | --- | --- | ---: | ---: | --- |
| deployment rollback | | | | | | | |
| shard failure | | | | | | | |
| retrieval explanation | | | | | | | |

Record observable behavior rather than interpretation. Useful notes include autocomplete use, query correction, evidence preview use, explanation inspection, hesitation, backtracking, and statements about trust. Do not record unrelated personal information.

## Session closeout

- Confirm the participant has no further questions and end the session.
- Run the complete study-wave capture command into restricted local storage.
- Compare telemetry event counts with this observer record; document discrepancies rather than editing raw events.
- Run the export validator, PMF report, and dashboard against the approved export.
- Preserve the preflight record, manifest, export files, observer record, code commit, corpus/query-set versions, and analysis outputs together.
- Redact sensitive queries and qualitative notes before any sharing or publication.

## Wave-01 decision rules

- Treat wave-01 as exploratory usability and product learning, not a statistically powered product-market-fit claim.
- Do not change the corpus, task wording, query set, or ranking configuration within a wave unless the change is recorded as a new version.
- Report all completed and failed tasks, including contradictory feedback and zero-result searches.
- Use medians and distributions for time measures; do not rely on a small-sample mean alone.
- Stop and review the protocol if consent, telemetry gating, provenance, or privacy checks fail.
