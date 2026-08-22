# NEBULA ANN scalability benchmark

`com.nebula.evaluation.AnnScaleBenchmark` is a separate systems benchmark for exact cosine search versus the custom HNSW implementation. It uses deterministic synthetic unit vectors so that scale mechanics can be measured without confusing ANN performance with semantic retrieval quality.

## Example run

After compiling Java sources:

```powershell
java -Xms2g -Xmx8g -cp build/classes com.nebula.evaluation.AnnScaleBenchmark `
  reports/generated/ann-scale.json `
  10000,100000 `
  128 `
  50 `
  10 `
  64 `
  20260822
```

The benchmark records vector count, dimension, seed, build time, heap delta, Recall@k, and exact/HNSW p50, p95, and p99 query latency. Run 1M vectors only on a machine with sufficient memory and record the hardware in the experiment manifest.

## Parameter study

Repeat the benchmark while varying `M`, `efConstruction`, and `efSearch`. The current harness uses M=16 and efConstruction=200 internally; parameterization of those values is a follow-up hardening task before publication experiments.

## Interpretation boundary

The benchmark measures ANN recall/latency/resource trade-offs on synthetic vectors. It does not establish semantic relevance quality, user benefit, or production capacity. Those require a separate embedding-quality benchmark and documented hardware/topology.

## Current status

The scalable harness is implemented but large-scale runs are **NOT YET MEASURED** in this repository.

