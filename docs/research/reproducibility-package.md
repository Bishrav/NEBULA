# Reproducibility package

Every substantive experiment should produce three layers of artifacts:

1. raw measurements that are never manually edited;
2. processed CSV/JSON/Markdown tables generated from the raw data;
3. a checksum-backed manifest recording inputs, code revision, dirty-tree
   status, runtime, operating system, architecture, processor, and CPU count.

Generate a general experiment manifest with
`tools/generate_experiment_manifest.py`. Verify it before analysis with the
same tool's `--verify` mode. Generated results belong under `reports/generated`
and should remain ignored unless a deliberately selected, provenance-safe
summary is being published.

## Evidence labels

Use these labels in machine-readable artifacts and documentation:

- `MEASURED`: the declared experiment completed and the raw artifact exists;
- `NOT_YET_MEASURED`: infrastructure or configuration exists, but no result is
  available;
- `HARDWARE-LIMITED`: the declared run could not complete on the recorded
  hardware;
- `HUMAN_ANNOTATION_REQUIRED`: labels or adjudication are still missing;
- `INSUFFICIENT_EVIDENCE`: an artifact exists but cannot support the intended
  claim.

Never replace a missing measurement with an estimated value. Keep model choice,
corpus/query/qrels versions, configuration, seed, timestamp, and manifest tied
to every published table.
