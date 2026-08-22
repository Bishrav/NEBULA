# NEBULA ranking model and trust-related signals

## Scientific terminology

NEBULA uses **trust-oriented** or **evidence-aware retrieval**, not a truth estimator. The system does not determine whether a document is true, safe, or institutionally authoritative. It combines observable, configurable signals that may help an engineer decide what to verify first.

The signals are kept conceptually separate:

| Concept | Current observable | Range | Interpretation | Failure mode / bias risk |
| --- | --- | ---: | --- | --- |
| Relevance | BM25 lexical score or vector similarity | Raw, then per-query normalized | Match between query and document content | Exact identifiers can favour lexical retrieval; semantic hashing is not a language model. |
| Semantic similarity | Cosine similarity from the configured embedding model | `[-1, 1]` before normalization | Content similarity under the embedding representation | Model and representation bias; similar text is not necessarily a correct answer. |
| Freshness | Exponential decay of days since last verification | `[0, 1]` | Temporal recency relative to a fixed evaluation timestamp | Newer is not always more correct; old canonical documents may be unfairly suppressed. |
| Source authority | Versioned metadata field supplied by the corpus owner | `[0, 1]` | A documented source-priority proxy | Subjective or manipulated metadata can create authority bias. |
| Graph authority | PageRank over extracted document links | Raw, then candidate-set normalized | Structural connectedness to the corpus | Popularity and link density can be mistaken for reliability; link spam is possible. |
| Evidence | Returned source path, title, score components, and available preview | Structured output | Supports user verification of the result | Score transparency is not the same as faithful or human-useful explanation. |

## Hybrid lexical-semantic fusion

For a query (q), NEBULA retrieves a candidate union from BM25 and semantic search. For each channel (c), the current implementation applies candidate-set min-max normalization:

\[
\hat{x}_c(d,q) =
\begin{cases}
0 & d \notin C_c(q) \\
1 & \max(C_c)=\min(C_c) \\
\frac{x_c(d,q)-\min(C_c)}{\max(C_c)-\min(C_c)} & \text{otherwise}
\end{cases}
\]

The weighted fusion score is:

\[
H(d,q) = w_L\hat{L}(d,q) + w_S\hat{S}(d,q)
\]

The default configuration is (w_L=0.5), (w_S=0.5). All weights are normalized and serializable through `RankingConfiguration`. Candidate-set normalization is a known methodological limitation: identical raw scores can receive different normalized values for different queries. RRF is a required Phase 4 comparison, not yet implemented.

## Trust-oriented score

The current trust-oriented ranker operates on BM25 candidates. Let:

- (hat{L}(d,q)) be candidate-set min-max normalized BM25;
- (A(d)) be the supplied source-authority metadata, defaulting to `0.5` when missing;
- (F(d,t)) be freshness at fixed evaluation time (t), with half-life (h):
  \[
  F(d,t)=2^{-\operatorname{ageDays}(d,t)/h}
  \]
- (hat{G}(d)) be PageRank normalized over the returned candidate set, defaulting to `0.5` when no graph is supplied.

The implemented configuration is:

\[
T(d,q)=w_L\hat{L}(d,q)+w_AA(d)+w_FF(d,t)+w_G\hat{G}(d)
\]

The default values are:

| Weight | Default |
| --- | ---: |
| `w_L` lexical | `0.70` |
| `w_A` source authority | `0.14` |
| `w_F` freshness | `0.10` |
| `w_G` graph authority | `0.06` |

The public compatibility API accepts a combined authority weight and explicitly splits it 70% to source metadata and 30% to graph authority. New experiments should use component weights directly. The weights sum to one after configuration normalization, and the configuration is emitted as versioned JSON.

## What the score does not mean

`T(d,q)` is not probability of truth, factual accuracy, safety, or institutional endorsement. It is a ranking utility based on observable proxies. A document can be authoritative but stale, fresh but weakly sourced, highly linked but wrong for the query, or semantically similar but non-answering. These conflicts are required robustness fixtures for the research evaluation.

## Configuration and reproducibility

The implementation class is `com.nebula.search.RankingConfiguration`. The experiment configuration should be serialized beside every result artifact and included in the experiment manifest. A fixed configuration is necessary but not sufficient for reproducibility; the corpus, qrels, split, code revision, evaluation timestamp, embedding model, runtime, and hardware also need to be recorded.

