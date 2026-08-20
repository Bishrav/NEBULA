# Research export validation

Run the validator before opening an export in the PMF notebook or using it in a paper:

```powershell
python tools/validate_research_export.py .\data\nebula-research.csv
```

For CI, scripts, or a notebook preflight, use JSON output:

```powershell
python tools/validate_research_export.py .\data\nebula-research.csv --json
```

The validator checks:

- the complete versioned CSV schema and supported event types;
- required values, integer ranges, timestamps, booleans, and confidence ratings;
- search, feedback, task, and observation event-specific fields;
- duplicate task events and task completions without a matching start;
- session counts and event counts for the study-wave record.

A valid export is structurally usable, not automatically publishable evidence. Researchers must still document participant recruitment, consent, corpus and query-set versions, the NEBULA commit, exclusions, and the analysis limitations described in the pilot protocol.

## Generate a PMF report

After validation, generate the reader-facing Markdown report. The command refuses invalid exports and labels the bundled fixture as synthetic:

```powershell
python tools/generate_pmf_report.py .\data\nebula-research.csv --output .\reports\generated\pmf-report.md --manifest .\data\nebula-research-manifest.json
```

The manifest argument is optional but recommended for real study waves. Save the response from `/v1/research/manifest` beside the export. Use `--generated-at` when a byte-for-byte reproducible report is required.

## Capture a complete study-wave bundle

With the persistent API running, capture all research artifacts in one command:

```powershell
python tools/capture_research_bundle.py --output-dir .\data\study-waves\wave-01 --captured-at 2026-08-20T00:00:00+00:00
```

The bundle contains the CSV and JSON exports, manifest, validation result, generated PMF report, and `capture.json` provenance record. The command fails if the manifest lacks study provenance or if the CSV fails validation. Real bundles belong in protected local storage and must not be committed to Git.

## Generate the experiment dashboard

Create a portable, read-only comparison dashboard from a validated export:

```powershell
python tools/generate_pmf_dashboard.py .\data\nebula-research.csv --manifest .\data\nebula-research-manifest.json --benchmark .\reports\generated\benchmark.json --output .\reports\generated\pmf-dashboard.html
```

The dashboard compares ranking modes, useful-feedback rate, zero-result rate, median latency, offline benchmark metrics, and protocol task outcomes. The benchmark is optional; when supplied, it must be an `evaluation-v1` artifact from the reproducible ranking runner. The dashboard is a snapshot of the supplied files; it does not refresh from the API or establish causal impact.
