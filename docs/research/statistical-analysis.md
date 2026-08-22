# NEBULA statistical analysis protocol

The statistical tooling is designed for paired query-level comparisons. It is an analysis framework, not evidence by itself.

## Primary comparison

For each query (q), compare a variant metric with the same query's BM25 metric. The unit is the query, not an individual returned document. Report the mean and median paired difference, win rate, number of overlapping queries, and the full difference distribution.

## Uncertainty and tests

`tools/analyze_paired_metrics.py` produces:

- percentile paired-bootstrap 95% confidence intervals for the mean difference;
- a paired sign-permutation p-value that does not assume normality;
- mean and median effect sizes;
- query-level win rate;
- Holm-adjusted p-values across the tested variants.

The tool does not automatically declare a result significant. A researcher must assess the sample size, query construction, independence assumptions, test-set isolation, practical effect size, and whether the comparison was pre-specified. Small synthetic fixtures should be labelled descriptive even when a computational p-value is available.

## Multiple comparisons

Testing several variants against one baseline creates a multiple-comparison problem. Holm-Bonferroni correction controls the family-wise error rate under the usual assumptions and is reported alongside raw p-values. The correction does not repair a biased corpus, leaked test set, weak qrels, or underpowered study.

## Required final reporting

For a research release, report:

1. the metric and cutoff;
2. the exact query IDs and split manifest;
3. the number of paired observations;
4. the pre-specified baseline and variants;
5. mean and median differences with confidence intervals;
6. raw and Holm-adjusted p-values only when inferential claims are justified;
7. effect sizes and practical relevance;
8. all failed, tied, and contradictory query cases;
9. the random seed and resampling counts;
10. the limitation that query-level observations may not represent all engineering information needs.

## Current status

The implementation and regression test exist. **No publication-grade statistical result has been established.** The current 4-document synthetic fixture remains insufficient for a superiority or significance claim.

