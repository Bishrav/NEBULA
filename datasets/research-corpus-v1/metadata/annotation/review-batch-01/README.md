# Review batch 01 blinded annotation handoff

Status: HUMAN ANNOTATION REQUIRED.

This batch contains two blinded packages with the same 47 queries and 188
query-document pairs, independently shuffled for annotators A and B. Give one
package to each annotator. They must work independently and must not compare
packages or share judgments.

For every row, the annotator must provide:

- relevance grade 0, 1, 2, or 3;
- uncertainty as true or false;
- a concise evidence note;
- an ISO-8601 annotated_at timestamp.

Return only the completed PSV packet files. Do not add names, emails, ranking
scores, source URLs, reviewer notes, category labels, or system guesses.

After receiving a submission, validate it locally with:

python tools/validate_annotation_submission.py --submission path\to\completed-submission.zip --packet datasets/research-corpus-v1/metadata/annotation/review-batch-01/blinded-annotation-package-A.zip --private-key data/review-batch-01-annotation-private/PRIVATE-query-id-key.psv --annotator A --output data/review-batch-01-annotation-private/annotations-A.canonical.psv

Use package B and annotator B for the second submission. Do not commit raw
human annotations or the private key. The next research stage is agreement
analysis and adjudication; qrels are not created at this stage.

## After both submissions arrive

Validate each returned submission against its package and the private query key:

python tools/validate_annotation_submission.py --submission path\to\submission-A.zip --packet datasets/research-corpus-v1/metadata/annotation/review-batch-01/blinded-annotation-package-A.zip --private-key data/review-batch-01-annotation-private/PRIVATE-query-id-key.psv --annotator A --output data/review-batch-01-annotation-private/annotations-A.canonical.psv

python tools/validate_annotation_submission.py --submission path\to\submission-B.zip --packet datasets/research-corpus-v1/metadata/annotation/review-batch-01/blinded-annotation-package-B.zip --private-key data/review-batch-01-annotation-private/PRIVATE-query-id-key.psv --annotator B --output data/review-batch-01-annotation-private/annotations-B.canonical.psv

Then calculate agreement and produce the disagreement list:

python tools/analyze_canonical_annotation_agreement.py --annotations data/review-batch-01-annotation-private/annotations-A.canonical.psv data/review-batch-01-annotation-private/annotations-B.canonical.psv --output data/review-batch-01-annotation-private/agreement.json

Do not create qrels until disagreements have been adjudicated and the explicit release gate is marked release-ready.
