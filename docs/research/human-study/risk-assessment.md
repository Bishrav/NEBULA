# Risk assessment

**DRAFT — REQUIRES INSTITUTIONAL/SUPERVISOR REVIEW**

| Risk | Likelihood | Impact | Mitigation | Stop/report trigger |
| --- | --- | --- | --- | --- |
| Participant fatigue or frustration | possible | low | 30-minute cap, breaks, voluntary stop | distress or request to stop |
| Entry of private/workplace information | possible | medium | synthetic corpus, briefing, warning text, redaction procedure | any sensitive entry |
| Data collected before consent | unlikely | high | fresh file, preflight, explicit consent gate, dry run | telemetry before consent |
| Re-identification through free text | possible | medium | avoid names, separate codes, redaction review, restricted access | identifying content found |
| Perceived evaluation of technical ability | possible | low | state that the tool is evaluated, not participant expertise | participant concern |
| Unauthorized data access | unlikely | high | access control, encryption, no Git/CI storage, deletion plan | access incident |
| Misleading trust signal or incorrect source | possible | medium | synthetic/public-safe tasks, verification rubric, clear non-truth-estimation statement | unsafe interpretation |

## Incident procedure

Stop the relevant task, preserve a minimal incident code without copying sensitive content, prevent further access, notify the supervisor/data owner, and follow the institution's incident-reporting process. Do not investigate or share participant content through GitHub issues or chat.

## Residual risk

The study does not eliminate the risk that a participant may enter information they should not share or that a search explanation may be misunderstood. Institutional review must determine whether the controls are adequate.
