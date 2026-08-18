# Phase 0 Validation Plan

## Questions to validate

1. Do engineering teams regularly fail to find internal technical information?
2. Which sources cause the greatest search friction?
3. Do users care about freshness and source authority?
4. Do explanations and evidence increase confidence in search results?
5. Would a self-hosted or privacy-first option change adoption decisions?

## Initial interview profile

Speak with engineers, tech leads, platform engineers, DevOps engineers, and engineering managers from teams with 10–100 engineers.

Target: 8–12 conversations before expanding the feature scope.

## Initial evaluation corpus

Create a versioned corpus containing realistic engineering documents:

- Architecture decision records
- API documentation
- Deployment runbooks
- Incident reports
- Service README files
- Troubleshooting guides
- Project plans

Do not use private customer data in the repository. Use synthetic, public, or explicitly consented data.

## Initial query set

Create at least 30 labelled queries across exact terminology, natural-language questions, phrase searches, freshness-sensitive queries, ambiguous queries, conflicting documents, and queries with no valid answer.

Each query should record relevant documents, relevance grade, freshness expectation, and whether evidence is sufficient.

## Success signals

- At least three repeated pain patterns across interviews.
- At least 30 labelled queries.
- BM25 produces useful results for the majority of baseline queries.
- Users can explain why a result is trustworthy.
- At least one pilot user wants to continue using the system.
