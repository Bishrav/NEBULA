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

The first research release should target at least 100 documents across multiple projects and documentation types, subject to licensing and acquisition feasibility. It should report the actual counts by project, source type, version, authority category, and topic rather than promising a balanced sample in advance.

## Known threats to validity

- Official documentation may overrepresent canonical, well-maintained projects.
- License-compatible sources may still have different writing styles and topic distributions.
- Page-level snapshots can contain duplicated navigation or generated content.
- Source authority categories are researcher-defined observables, not truth labels.
- The corpus may not represent proprietary engineering teams or incident documentation.

## Current status

The source registry and admission contract exist. **No public source snapshot has yet been admitted to the NEBULA repository.** The existing four-document synthetic fixture remains the only checked-in evaluation corpus.

