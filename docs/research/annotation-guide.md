# NEBULA relevance annotation guide

This guide defines labels for the fixed evaluation query set. Freeze the guide and query set before measuring annotator agreement. Annotators should work independently, without seeing another annotator's labels or the ranking output being evaluated.

Store records using `benchmarks/evaluation/annotations-v1.template.psv` with the columns `query_id|source_path|annotator_id|grade|evidence_note`. Keep completed files in protected study storage and validate them with `python tools/validate_annotations.py <annotations.psv> --queries benchmarks/evaluation/queries-v1.psv --corpus-dir benchmarks/evaluation/corpus-v1`. The validator checks identifiers, source paths, grades, evidence notes, and duplicate annotator records; it does not detect personal information, so a human privacy review remains required.

## Unit of annotation

For each query–document pair, judge whether the document would help an engineer answer the information need stated by the query. Read enough of the document to judge the claim; do not infer missing evidence from the filename or title alone.

## Relevance grades

| Grade | Meaning | Decision rule |
| ---: | --- | --- |
| 3 | Direct answer | The document directly answers the need and contains the evidence an engineer would use. |
| 2 | Substantial support | The document materially helps answer the need but requires another source, interpretation, or missing detail. |
| 1 | Useful context | The document is related and may orient the search, but does not support the answer by itself. |
| 0 | Not relevant / hard negative | The document does not answer the need, even if it shares terminology. |

For no-valid-answer queries, label documents 0 unless the document genuinely supports the stated need. Do not assign a positive grade merely because the query is syntactically similar to a document.

## Adjudication

Use at least two independent annotators for a research release. Calculate agreement on the full label set before adjudication; report the agreement statistic and the number of disagreements. Discuss disagreements using the written rules, record the adjudicated label and reason, and preserve the pre-adjudication labels.

Recommended records:

- annotator ID pseudonym;
- query-set and guide versions;
- document and query identifiers;
- independent grade;
- disagreement reason;
- adjudicated grade and adjudicator;
- timestamp and code revision.

After validation, calculate pairwise agreement with `python tools/analyze_annotation_agreement.py <annotations.psv> --queries benchmarks/evaluation/queries-v1.psv --corpus-dir benchmarks/evaluation/corpus-v1 --output <agreement.json>`. The output reports overlap, raw agreement, nominal Cohen's kappa, linear weighted kappa, quadratic weighted kappa, and disagreement counts for each annotator pair. Weighted kappa treats a 3-versus-2 disagreement as less severe than a 3-versus-0 disagreement. Agreement statistics are descriptive quality checks, not proof that the rubric is valid.

Generate a non-destructive adjudication packet with `python tools/generate_adjudication_packet.py <annotations.psv> --queries benchmarks/evaluation/queries-v1.psv --corpus-dir benchmarks/evaluation/corpus-v1 --output <packet.json>`. Each disagreement includes all independent labels and blank adjudicator fields. A qualified adjudicator must complete those fields; the tool never selects a label automatically.

Before using labels in a research claim, run `tools/validate_annotation_release.py` with the validated annotations, agreement JSON, and completed adjudication packet. The gate requires two annotators, an agreement pair report, and non-empty adjudicator decisions for every disagreement. It is a release gate, not a substitute for ethics review or methodological judgment.

Do not use participant telemetry to silently change offline relevance labels. If a label changes after the pilot, create a new query-set version and rerun the benchmark.

## Quality checks

- [ ] Every query has at least one documented positive judgement or is explicitly marked as no-answer in study metadata.
- [ ] Every judgement references an existing corpus source path.
- [ ] No annotator sees ranking-system outputs during independent labelling.
- [ ] Positive labels include an evidence rationale in the annotation record.
- [ ] Disagreements and exclusions are reported rather than removed.
