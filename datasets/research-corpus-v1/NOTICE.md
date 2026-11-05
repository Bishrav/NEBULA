# NOTICE — research-corpus-v1

This dataset contains documentation text from the projects below. It is **not**
covered by the MIT License that applies to the NEBULA source code. Each source
keeps its own license and attribution requirements, listed here.

## Kubernetes documentation

- Creator / copyright: © The Kubernetes Authors
- Original material: https://github.com/kubernetes/website
  (files under `content/en/docs/`, commit `9b5eeefac89219bc2b1d6875c0d5bd7071e57ab6`)
- License: Creative Commons Attribution 4.0 International (CC BY 4.0)
  — https://creativecommons.org/licenses/by/4.0/
- License text retained in this dataset: `licenses/source-kubernetes-docs-LICENSE.txt`
- Warranty disclaimer: as stated in Section 5 of the license text above.

## PostgreSQL documentation

- Copyright: Portions Copyright (c) 1996-2026, PostgreSQL Global Development Group;
  Portions Copyright (c) 1994, The Regents of the University of California
- Original material: https://github.com/postgres/postgres
  (files under `doc/src/sgml/`, commit `e2c812f1475dc75ab6f8a39fb5696d8d32d05fa1`)
- License: PostgreSQL License — https://www.postgresql.org/about/licence/
- Full copyright notice and license text retained in this dataset:
  `licenses/source-postgresql-docs-COPYRIGHT.txt`
  (this file must accompany any copy or subset of the PostgreSQL material)

## Modifications

The `raw/` directory holds the upstream files as retrieved. The sole difference
is that line endings may have been converted from CRLF to LF
(known case: `content/en/docs/contribute/generate-ref-docs/metrics-reference.md`).

The `normalized/` directory holds **modified** versions of those files, produced
by `tools/acquire_research_corpus.py`:

- Kubernetes: YAML front matter, Hugo shortcode lines (for example `{{< note >}}`,
  `{{< figure >}}`, `{{% code_sample %}}`, `{{% thirdparty-content %}}`) and HTML
  comments were removed.
- PostgreSQL: SGML/XML markup was converted to Markdown-like text and entities
  were decoded.

Per-document provenance (upstream repository, commit, source URL, license,
attribution, checksums and transformation notes) is recorded in
`manifests/research-corpus-v1.json`.

Images, example files and shared includes referenced by some documents are not
part of this dataset.

## Trademarks and endorsement

Kubernetes® and PostgreSQL® and related names are trademarks of their respective
owners; neither license grants trademark rights. The creators and licensors named
above do not sponsor or endorse NEBULA or this dataset.
