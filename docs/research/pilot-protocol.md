# NEBULA pilot study protocol

## Purpose

This protocol turns the NEBULA PMF hypothesis into a small, repeatable user study. It is designed for exploratory university research and product learning, not as evidence of general population behavior.

## Research question

Can engineers find and verify an operational or architectural source faster and with greater confidence when NEBULA exposes lexical, semantic, hybrid, and trust-aware ranking signals?

## Study design

- Recruit 5–10 engineers or advanced computing students who regularly search technical documentation.
- Use one versioned corpus and the same corpus for every participant in a study wave.
- Give every participant the same three tasks: deployment rollback, shard failure response, and hybrid-retrieval explanation.
- Counterbalance the order of ranking modes when comparing modes, or keep BM25 as the baseline for a focused PMF study.
- Allow a maximum of five minutes per task and record task completion, first useful source, reformulations, and verification behavior.
- Collect session telemetry only after explicit consent; do not collect names, emails, account identifiers, or unrelated browsing data.

## Session procedure

1. Read the participant the consent summary and answer questions.
2. Start the API with a fresh telemetry file and record the corpus version, code commit, and study wave.
3. Open the search workspace in a clean browser profile.
4. Ask the participant to select `Start study session`, review the dialog, and explicitly consent before beginning tasks.
5. Give the participant one task at a time without suggesting query wording; use the task controls to record start and completion.
6. Ask the participant to explain why they trusted or rejected the first useful source.
7. Record observer notes separately from the exported telemetry.
8. Export CSV and JSON, verify the event count, then close the session.

## Primary measures

| Measure | Operational definition |
| --- | --- |
| Task success | Participant identifies a source judged relevant by the study rubric within the task limit |
| Time to first useful source | Seconds from first query to opening or accepting a useful source |
| Reformulation count | Additional search submissions before the first useful source |
| Verification confidence | Participant rating from 1 (not confident) to 5 (very confident) |
| Useful feedback rate | Useful judgements divided by all submitted result judgements |
| Zero-result rate | Searches returning no indexed results divided by all searches |

## Analysis plan

Report participant count, corpus version, task completion rate, median rather than only mean time, reformulation distributions, zero-result rate, usefulness feedback, and qualitative explanation themes. Compare ranking modes within participants when sample size allows. Include failed searches and contradictory feedback in the appendix.

Do not claim statistical significance from a small convenience sample. Treat results as exploratory, state uncertainty, and separate retrieval performance from interface trust and usability.

## Privacy and retention

Use the anonymous browser-local session ID only to group events within a study session. Store telemetry on the study machine, restrict access to the research team, and delete raw exports according to the approved study plan. Never commit real participant exports to Git. If queries contain sensitive operational information, redact or replace them before publication.

## Reproducibility record

For each study wave, preserve the corpus identifier, query/task sheet version, NEBULA commit SHA, API configuration, telemetry export filenames, and notebook commit. The API manifest at `/v1/research/manifest` records the event schema used by the running build.
