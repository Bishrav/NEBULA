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
  16 `
  200 `
  64 `
  3 `
  20260822
```

The positional parameters after `cutoff` are `M`, `efConstruction`, `efSearch`, `repeats`, and `seed`. The benchmark records vector count, dimension, parameters, per-trial build time, a deterministic index-size estimate, JVM heap delta, Recall@1/5/10/k, and exact/HNSW p50, p95, and p99 query latency. Heap delta is a noisy JVM diagnostic; `estimatedIndexBytes` is a deterministic lower-bound estimate and must not be presented as resident memory.

Each positional parameter is independent: supplying `M` does not require
supplying the later parameters. The small regression run in
`AnnScaleBenchmarkTest` protects this command-line contract.

## Parameter study

The version-controlled matrix is `experiments/ann/parameter-matrix.json`. It covers 10K, 100K, and 1M vectors; 128 and 384 dimensions; M in 8/16/32; efConstruction in 100/200/400; efSearch in 20/50/100/200; and three repeated trials. Use `tools/report_ann_scale.py` to produce flat CSV and Markdown tables from each JSON run.

Create a run manifest after each benchmark:

```powershell
python tools/create_ann_manifest.py reports/generated/ann-scale.json `
  --output reports/generated/ann-scale-manifest.json `
  --status NOT_YET_MEASURED
```

The manifest records the artifact checksum, Git revision and dirty-tree state, Java/Python runtime information, operating system, architecture, processor, and CPU count.

## Interpretation boundary

The benchmark measures ANN recall/latency/resource trade-offs on synthetic vectors. It does not establish semantic relevance quality, user benefit, or production capacity. Those require a separate embedding-quality benchmark and documented hardware/topology.

## Current status

The configurable, repeated-trial harness and report converter are implemented. The publication-scale matrix is **NOT YET MEASURED** in this repository. If 1M vectors cannot complete on the available machine, record `HARDWARE-LIMITED` in the manifest rather than inferring a result.
