# NEBULA corpus licensing review

Status: CONDITIONAL, NOT FINAL — decisions recorded 2026-11-05 (both sources
APPROVED_WITH_ATTRIBUTION), but release conditions R1–R3 below remain open
Repository state is unchanged and correct: `legal_clearance: NOT_DECIDED`,
`provisional: true`. Do not change either until R1–R3 are complete.
Current machine audit: STRUCTURAL PASS (`tools/audit_corpus_licensing.py`), legal clearance NOT_DECIDED
Audit artifact: data/review-batch-01-license-audit.json

This checklist records the human review needed before the research corpus is
described as a final public release. It is not a legal opinion.

Audited revision: `main` @ `7947b12afedcd2953ab67f142d17ffe340240b97`.

## Corpus scope

- Corpus: research-corpus-v1
- Documents: 843
- Sources: Kubernetes documentation (595 documents) and PostgreSQL documentation (248 documents)
- Transformations: raw upstream files are retained; normalized text removes deterministic formatting artifacts and records checksums in the manifest.
- Registry: benchmarks/research/source-registry-v1.psv
- Manifest: datasets/research-corpus-v1/manifests/research-corpus-v1.json

## Source review

### Kubernetes documentation

- Declared license: CC-BY-4.0
- Attribution: The Kubernetes Authors
- Evidence: https://github.com/kubernetes/website/blob/main/LICENSE
- Retained notice: datasets/research-corpus-v1/licenses/source-kubernetes-docs-LICENSE.txt
- Scope: content/en/docs/**/*.md and Markdown-compatible documentation files
- Reviewer checks:
  - [x] Confirm the license text matches the pinned upstream commit.
    SHA-256 `9ba9550ad48438d0836ddab3da480b3b69ffa0aac7b7878b5a0039e7ab429411`
    is identical for the retained file, upstream `LICENSE` at pinned commit
    `9b5eeefac89219bc2b1d6875c0d5bd7071e57ab6`, and upstream `main`.
    The repository page lists CC-BY-4.0. kubernetes.io footer reads
    "© 2026 The Kubernetes Authors | Documentation Distributed under CC BY 4.0",
    which supports the recorded attribution string.
  - [ ] Confirm the retained attribution is sufficient for redistribution.
    OPEN → R1. Attribution is recorded per document in the manifest, in
    `metadata/corpus-report.md` and in `metadata/license-attribution-summary.json`,
    and the license text is retained. There is no corpus-level NOTICE and the
    normalized files carry no attribution or modification statement. CC BY 4.0
    §3(a)(1)(B) requires an indication that the material was modified, and
    §3(a)(2) allows this to be met by a reasonable pointer to a resource that
    contains it.
  - [x] Check admitted paths for third-party notices, embedded assets, or generated material.
    Automated scan of all 595 raw files (not a line-by-line human read):
    - No binary assets; the corpus holds only text files.
    - 6 files use Markdown images and 2 use `<img>`; these are links to
      `/images/...`, and the image files are not in the corpus.
    - 56 files use `figure`, `include` or `code_sample` shortcodes whose targets
      (images, example YAML, shared includes) are not in the corpus.
    - Base64-looking strings are Mermaid diagram sources
      (`contribute/style/diagram-guide.md`), sample JWTs
      (`reference/access-authn-authz/authentication.md`) and digits of pi (`job.md`).
    - The two `kubernetes-api.md` pages are narrative text, not generated OpenAPI reference output.
    - No third-party copyright or license notices were found. The only
      "Copyright" hits are Kubernetes' own contribution guidance.
    - 33 files mention "third-party" in ordinary prose.
  - [ ] Confirm normalized derivatives preserve required attribution context.
    OPEN → R1. Attribution context lives in the manifest and metadata, not inside
    the normalized files themselves.

### PostgreSQL documentation

- Declared license: PostgreSQL License
- Attribution: PostgreSQL Global Development Group
- Evidence: https://www.postgresql.org/about/licence/
- Retained notice: datasets/research-corpus-v1/licenses/source-postgresql-docs-COPYRIGHT.txt
- Scope: doc/src/sgml/**/*.sgml and XML-compatible documentation files
- Reviewer checks:
  - [x] Confirm the license and COPYRIGHT text match the pinned upstream commit.
    SHA-256 `3d6af92ff8a4c2cdf69afb1cf44edea727922f5cd0cf8b5f72b11cdecac8fdfd`
    is identical for the retained file and upstream `COPYRIGHT` at pinned commit
    `e2c812f1475dc75ab6f8a39fb5696d8d32d05fa1`. Against the official licence page
    the grant and both disclaimer paragraphs are identical; only cosmetic
    wording differs ("©" vs "(c)", "The PostgreSQL Global…", "also known as").
  - [ ] Confirm the retained attribution is sufficient for redistribution.
    OPEN → R1. The grant requires the copyright notice and the two disclaimer
    paragraphs to appear "in all copies". The full notice is retained under
    `licenses/` and the PostgreSQL legal notice (`legal.sgml`) is in both the raw
    and normalized sets (all license paragraphs verified present). The notice
    must travel with any redistributed subset or export bundle.
  - [x] Check admitted paths for third-party notices, generated material, or bundled examples.
    Automated scan of all 248 raw files:
    - The only copyright statements are PGDG and the Regents of the University
      of California, the same holders as the license (`legal.sgml`, and an
      example source header in `lobj.sgml`).
    - Mentions of BSD, libedit and FreeBSD are descriptive prose
      (`installation.sgml`, `client-auth.sgml`, `pgcrypto.sgml`), not embedded third-party material.
    - 5 files reference `images/*.svg` figures that are not in the corpus.
    - No generated output or bundled third-party examples were found.
  - [ ] Confirm normalized derivatives preserve required attribution context.
    OPEN → R1. `legal.sgml` keeps the full notice after normalization; other
    normalized files rely on the manifest.

### Cross-source checks (registry, provenance, transformations)

- [x] Source scope matches the registry. 595 Kubernetes files, all `.md`, all under
  `content/en/docs/`; 248 PostgreSQL files, all `.sgml`, all under `doc/src/sgml/`;
  0 out-of-scope paths. One pinned commit per source; every `source_url` contains it.
  7 documents rejected as "empty after normalization" and not admitted.
- [x] Retained raw files are upstream files. 842 of 843 are byte-identical to upstream
  at the pinned commits. 1 differs only in line endings (CRLF upstream, LF retained):
  `content/en/docs/contribute/generate-ref-docs/metrics-reference.md`.
- [x] Normalized copies are only transformations of the retained raw files.
  Re-running `normalize()` from `tools/acquire_research_corpus.py` on every raw file
  reproduces the stored normalized text exactly (843/843), and all 843
  `normalized_checksum` values match. Normalization strips front matter, Hugo
  shortcode lines (including 19 `thirdparty-content` disclaimer lines) and HTML
  comments (Kubernetes), and converts SGML markup to Markdown-like text (PostgreSQL).
- [x] Required license notices remain in the repository. Both retained files are
  byte-identical to the uploaded files. The root README states that code is MIT and
  that imported corpus content keeps its upstream licenses.
- [ ] Provenance integrity. OPEN → R2. All 843 `content_checksum` values match the
  CRLF form of the raw file, not the committed LF bytes (acquisition ran on a
  Windows checkout; git normalized to LF on commit). Not a licensing problem, but the
  manifest cannot be verified as written. `tools/validate_research_corpus.py`
  (around lines 46–52) does not catch it, because the raw-checksum branch tests
  `field == "raw_path"` inside a loop over the checksum fields.

## Decision record

| Source | Decision | Reviewer | Date | Notes |
| --- | --- | --- | --- | --- |
| Kubernetes documentation | APPROVED_WITH_ATTRIBUTION | Bishrav Shiwakoti | 2026-11-05 | CC BY 4.0. License verified at pinned commit; scope, raw and normalized provenance verified; no embedded assets or third-party notices found by scan. Conditions R1, R2, R3. |
| PostgreSQL documentation | APPROVED_WITH_ATTRIBUTION | Bishrav Shiwakoti | 2026-11-05 | PostgreSQL License. COPYRIGHT verified at pinned commit; notice retained and present in `legal.sgml`. Conditions R1, R2, R3. |

Allowed decisions: APPROVED, APPROVED_WITH_ATTRIBUTION, BLOCKED, or PENDING.

### Release conditions

- **R1 — Corpus NOTICE.** Add `datasets/research-corpus-v1/NOTICE.md` (draft provided)
  carrying creator attribution, copyright notice, license references, the
  modification statement and the no-endorsement/trademark note. It must ship with
  every export (publication package, mirrors, subsets).
- **R2 — Checksum integrity (COMPLETED).** The committed corpus now has
  datasets/research-corpus-v1/raw/** -text in .gitattributes, the validator
  checks raw bytes without line-ending normalization, and a regression test guards
  this behavior. The manifest currently validates all 843 raw and normalized
  checksums. The known metrics-reference.md upstream line-ending difference is
  recorded in this review.
- **R3 — Update status fields together.** After R1 and R2 are merged, update the
  registry rows (currently `ACCEPTED`, with notes that still say review is
  pending), `licensing_status` / `provisional` in the manifest, `status` in
  `metadata/license-attribution-summary.json`, and the README banner. Also pin the
  Kubernetes `license_evidence_url` to commit `9b5eeef…` instead of `main`.

The corpus must remain provisional while any source is PENDING or BLOCKED.
After review, update the registry notes and the corpus release metadata with the
decision and preserve the reviewer identity outside public research artifacts
when required by the study data-management plan.

### Limits of this review

The third-party and embedded-asset checks are pattern scans over raw text, not a
human read of 843 documents. Quoted third-party text, logos or trademarks inside
prose would not be caught. Trademarks are not licensed by either source license
(CC BY 4.0 §2(b)(2)).
