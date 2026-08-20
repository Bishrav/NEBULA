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
