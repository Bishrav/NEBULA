# NEBULA pilot session checklist

## Before the participant

- [ ] Confirm consent wording and study task sheet.
- [ ] Start the API with study version, corpus version, study wave, query-set version, and NEBULA commit SHA metadata.
- [ ] Start the API with a new local telemetry file.
- [ ] Run `python tools/pilot_preflight.py --output .\data\study-waves\wave-01\preflight.json` and confirm it reports `READY`.
- [ ] Save the manifest response with the export as the study-wave provenance record.
- [ ] Open a clean browser profile.
- [ ] Run the dry-run harness on a fresh telemetry file before the first participant: `python tools/run_pilot_dry_run.py --output-dir .\data\study-waves\wave-01\dry-run`.

## During the session

- [ ] Do not suggest query wording or ranking mode choices.
- [ ] Record task start and completion times in observer notes.
- [ ] Record reformulations and the first useful source.
- [ ] Start and complete each study task in the task panel, marking success only from the agreed task rubric.
- [ ] Ask for confidence and the reason for trusting the source.
- [ ] Note autocomplete, evidence-preview, correction, and explanation behavior.

## After the session

- [ ] Export both CSV and JSON from `/v1/research/export`.
- [ ] Check that search and feedback counts match the session notes.
- [ ] Store exports outside Git with restricted access.
- [ ] Run `notebooks/nebula_pmf_analysis.ipynb` against the approved export.
- [ ] Redact sensitive queries before sharing or publishing.
