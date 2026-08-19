# NEBULA pilot telemetry data dictionary

The CSV export contains one row per persisted search or feedback event. JSON preserves the same event objects.

The live API publishes this schema as a machine-readable manifest at `/v1/research/manifest`.

| Field | Search | Feedback | Meaning |
| --- | --- | --- | --- |
| `type` | yes | yes | `search` or `feedback` |
| `sessionId` | yes | yes | Anonymous browser-local grouping key |
| `timestamp` | yes | yes | Unix epoch milliseconds recorded by the API |
| `query` | yes | yes | Query text submitted or judged |
| `mode` | yes | yes | Ranking mode used for the event |
| `results` | yes | no | Number of results returned |
| `latencyNanos` | yes | no | Server-side search duration in nanoseconds |
| `documentId` | no | yes | Judged result identifier |
| `sourcePath` | no | yes | Judged result source path |
| `useful` | no | yes | Participant judgement: `true` or `false` |

Blank fields are expected where a field does not apply to the event type. The sample fixture is synthetic. Real exports must be handled as research data and must not be committed to the repository.
