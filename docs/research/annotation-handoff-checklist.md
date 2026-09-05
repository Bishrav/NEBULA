# Human annotation handoff checklist

Status: `HUMAN ANNOTATION REQUIRED`

The intended handoff contains two sanitized packages with the same 3,790
query-document pairs from a protected 379-query source and frozen research
corpus. Those packages are not present in this checkout and must not be
described as generated until the protected inputs are supplied. Human labels
must be collected independently before agreement analysis or qrels creation.

## Before sending

- Confirm the query wording and corpus snapshot are frozen.
- Send only one package to each annotator.
- Send `blinded-annotation-package-A.zip` to annotator A.
- Send `blinded-annotation-package-B.zip` to annotator B.
- Do not send `PRIVATE-query-id-key.psv`.
- Do not send `retrieval-candidates.psv` or `retrieval-manifest.json`.
- Do not tell annotators which retrieval system produced the candidates.
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

No human labels, agreement statistic, adjudication result, qrels, or retrieval
quality claim exists yet. Until two valid submissions are received and
reviewed, the research status remains `PARTIAL` and `HUMAN ANNOTATION REQUIRED`.
