# NEBULA Product-Market-Fit Validation

## Product hypothesis

Small and medium-sized engineering teams will adopt a self-hostable knowledge search tool if it reduces time spent finding operational and architectural evidence while making source quality visible.

## First user workflow

1. Ask a realistic engineering question.
2. Review the top five sources.
3. Compare lexical, semantic, and hybrid modes.
4. Inspect the ranking signals.
5. Report whether the first useful source was found and whether the explanation increased trust.

## Interview tasks

- Find the deployment rollback procedure.
- Find the documented response to a shard failure.
- Decide whether a search result is authoritative enough to use in an incident.
- Explain why the first result ranked above the second result.

## Metrics

| Metric | Definition | Initial target |
| --- | --- | --- |
| Time to first useful source | Seconds from query submission to a source the user says they would open | under 30 seconds |
| First-result usefulness | Sessions where the first result is useful without reformulating | at least 60% |
| Explanation comprehension | Users who can correctly identify at least one ranking signal | at least 70% |
| Search reformulation rate | Queries requiring another query before a useful source is found | below 40% |
| Weekly repeat intent | Pilot users who say they would use the tool weekly | at least 50% |

These targets are discovery targets, not validated claims. Record task context, query text, corpus version, ranking mode, and user feedback for every session.

The local pilot records result feedback through `POST /v1/feedback` and exposes the in-memory aggregate at `GET /v1/metrics/feedback`. These metrics reset when the API restarts and must not be treated as durable analytics or user identity data.

Autocomplete interactions should be observed during usability sessions: record whether suggestions reduce reformulation or help users discover the vocabulary they need.

The API also exposes session search telemetry at `GET /v1/metrics/search`, including total searches, zero-result searches, average latency, and counts by ranking mode. These values are in-memory pilot metrics and reset on restart.

The search workspace renders these telemetry values beside the results so pilot observers can record usability outcomes without inspecting API responses manually.

## Research safeguards

Do not treat a small interview sample as proof of product-market fit. Separate retrieval quality from interface usability, preserve failed searches for error analysis, and obtain consent before collecting user queries or identifying information.
