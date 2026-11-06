# Human annotation handoff checklist

Status: `RELEASE VALIDATED IN PROTECTED STORAGE`

The completed review batch contains two sanitized packages with the same 188
query-document pairs: 47 queries with one grounding document and three seeded
random same-project distractors each. The packages are not present in this
checkout. Human labels must be collected independently before agreement
analysis or qrels creation; this constrained packet is not a corpus-wide
relevance assessment.

## Before sending

- Confirm the query wording and corpus snapshot are frozen.
- Send only one package to each annotator.
- Send `blinded-annotation-package-A.zip` to annotator A.
- Send `blinded-annotation-package-B.zip` to annotator B.
- Do not send `PRIVATE-query-id-key.psv`.
- Do not send `retrieval-candidates.psv` or `retrieval-manifest.json`.
- Do not reveal the grounding-document identity or candidate-construction
  metadata to annotators.
- Keep annotator identities and contact details outside the annotation files.

## Instructions to annotators

For every row, independently enter:

- `relevance_grade_0_3`: 0, 1, 2, or 3;
- `uncertainty`: `true` or `false`;
- `evidence_note`: a short reason grounded in the displayed excerpt;
- `annotated_at`: an ISO-8601 timestamp.

Do not change query IDs, document IDs, query text, titles, excerpts, or column
names. Do not add ranks, scores, URLs, trust values, grounding IDs, or system
names. Do not inspect the other annotator's file before submitting.

## After receiving a submission

Store each completed ZIP in protected research storage. Do not commit completed
annotations, participant information, or the private key to Git. Validate each
submission separately:

```powershell
python tools/validate_annotation_submission.py `
  --submission C:\protected\annotator-A-completed.zip `
  --packet reports/generated/annotation-packages/blinded-annotation-package-A.zip `
  --private-key reports/generated/annotation-private/PRIVATE-query-id-key.psv `
  --annotator A `
  --output C:\protected\annotation-A.canonical.psv
```

Repeat with B and the B package. Only if both validations pass should the
canonical exports be supplied to agreement analysis. Preserve the original
submitted files unchanged.

## Current evidence boundary

The protected review batch has two validated canonical exports, zero
disagreements, completed consensus adjudication, and a release-ready qrels
file. The protected labels and qrels must remain outside the public repository.
This does not by itself authorize a final held-out claim or a human-study
claim; the agreement result is descriptive for the four-candidate packet, and
final configuration freezing and institutional review remain separate gates.
