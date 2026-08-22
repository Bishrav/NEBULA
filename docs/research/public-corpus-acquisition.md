# Public engineering corpus acquisition plan

## Objective

Build a realistic, public-safe engineering documentation corpus without confusing public accessibility with permission to redistribute. The corpus is a research input, not a product feature and not a claim about the quality of any source.

## Proposed source families

The initial registry contains two proposed official documentation families:

| Source | License evidence | Proposed scope | Status |
| --- | --- | --- | --- |
| Kubernetes website documentation | [Kubernetes website repository](https://github.com/kubernetes/website/) identifies the documentation repository as CC BY 4.0. | English documentation pages about configuration, deployment, networking, observability, and security. | Proposed; attribution and third-party-content review required. |
| PostgreSQL documentation | [PostgreSQL license](https://www.postgresql.org/about/licence/) grants permission to use, copy, modify, and distribute documentation subject to its notice terms. | Versioned manuals about administration, replication, configuration, performance, and migration. | Proposed; preserve version and notice information. |

The registry is deliberately small. Additional sources should be admitted only after checking their repository terms, documentation-specific license, third-party excerpts, robots/terms constraints where relevant, and redistribution obligations.

## Acquisition protocol

1. Record the source registry row and the exact retrieval date.
2. Acquire from the official repository or official download endpoint, retaining the upstream commit/tag/version.
3. Convert only the documented source formats; do not silently rewrite substantive content.
4. Preserve source URL, license, attribution, upstream version, retrieval date, transformation description, and checksum.
5. Exclude private, authenticated, paywalled, unclear-license, user-generated, or third-party content unless separately cleared.
6. Run `tools/validate_corpus_manifest.py` and conduct a human licensing/attribution review.
7. Freeze the admitted corpus before query authoring and annotation.

## Corpus composition target

The first research release targets at least 500 documents, with 1,000+ preferred if licensing and storage remain practical. The first deterministic snapshot currently contains 843 admitted documents: 595 Kubernetes documents and 248 PostgreSQL documents. It reports actual counts by project, source type, version, license, and word count rather than promising a balanced sample in advance.

## Known threats to validity

- Official documentation may overrepresent canonical, well-maintained projects.
- License-compatible sources may still have different writing styles and topic distributions.
- Page-level snapshots can contain duplicated navigation or generated content.
- Source authority categories are researcher-defined observables, not truth labels.
- The corpus may not represent proprietary engineering teams or incident documentation.

## Current status

The source registry, acquisition tool, normalization rules, manifest validator, checksums, statistics report, and rejected-source report exist. `datasets/research-corpus-v1` contains a **provisional** 843-document snapshot from fixed upstream commits. It is marked **NEEDS HUMAN LICENSING REVIEW** for third-party notices, generated content, attribution, and redistribution obligations. The existing four-document synthetic fixture remains regression-only.
