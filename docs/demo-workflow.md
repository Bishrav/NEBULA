# NEBULA demo workflow

This workflow is designed for a short academic or technical review. It demonstrates the complete loop:

```text
question -> ranked sources -> ranking signals -> source evidence -> controlled comparison
```

It uses only the local synthetic fixture and does not require accounts, external services, or private documents.

## Before the demo

From the repository root, start the API:

```powershell
docker compose -f infrastructure/docker/compose.yaml up --build -d
Invoke-WebRequest http://127.0.0.1:8082/health/ready
```

In a second terminal, serve the interface:

```powershell
python -m http.server 5174 --directory .\apps\search-ui
```

Open [http://127.0.0.1:5174](http://127.0.0.1:5174).

## Validated BGE smoke path

The repository also includes an opt-in BGE hybrid configuration selected from
the validation sweep. It is intentionally demonstrated with a frozen query
whose embedding is present in the private offline cache; this keeps the demo
reproducible without downloading a model or contacting an external service.

After running the one-command launcher, select **BGE hybrid · validated
candidate** in the workspace and search for:

```text
In which Kubernetes versions and at what stage (alpha/beta/GA) is the DisableAllocatorDualWrite feature gate available?
```

The expected first source is the Kubernetes feature-gates reference document.
The result card should expose separate lexical, semantic, and hybrid scores.
This is a smoke test of the configured retrieval path, not a claim that BGE is
superior on a general workload.

The equivalent API check is:

```powershell
$query = 'In which Kubernetes versions and at what stage (alpha/beta/GA) is the DisableAllocatorDualWrite feature gate available?'
$encoded = [uri]::EscapeDataString($query)
Invoke-WebRequest "http://127.0.0.1:8082/v1/search?q=$encoded&mode=bge-hybrid&limit=5"
```

If the private cache is unavailable, the API reports an explicit error rather
than silently falling back to a different embedding model. Arbitrary live
queries are also outside this cached smoke path until a live query-embedding
provider is configured.

If the API is unavailable, the UI should show an explicit offline message. Stop the demo with:

```powershell
docker compose -f infrastructure/docker/compose.yaml down
```

The named telemetry volume is retained by Docker. Do not export or commit its contents unless the approved research workflow requires it.

## Five-minute walkthrough

### 1. Establish the baseline

Use the default **BM25 lexical** mode and search for:

```text
deployment rollback
```

Point out:

- result title and source path;
- deterministic score;
- per-term contributions;
- response time;
- the absence of a generated answer that cannot be checked.

This is the baseline against which other ranking modes should be discussed.

### 2. Compare ranking modes

Run the same query with:

- **Hybrid lexical + semantic**
- **Semantic exact**
- **Semantic HNSW**
- **Trust-aware**

Do not present one mode as “best” from this fixture alone. Instead, compare which documents move and which signals appear. The experiment question is whether the change is useful and robust on a properly judged corpus.

Suggested comparison table for notes:

| Mode | Top result | Signal visible | Reviewer observation |
| --- | --- | --- | --- |
| BM25 lexical |  | term contributions |  |
| Hybrid |  | lexical + semantic |  |
| Semantic exact |  | semantic channel |  |
| Semantic HNSW |  | ANN channel |  |
| Trust-aware |  | authority, PageRank, freshness |  |

### 3. Verify evidence

Open **Open evidence** on a result. Show that the interface retrieves the indexed source text rather than asking the reviewer to trust a summary.

Use:

```text
shard failure
```

Then open the most relevant source and explain how a reviewer can verify the result directly.

### 4. Demonstrate autocomplete and phrase search

Type:

```text
retriev
```

Use the keyboard arrows to select an autocomplete suggestion.

Then search for the exact phrase:

```text
"query coordinator"
```

This demonstrates the difference between ordinary term matching and positional phrase retrieval.

### 5. Demonstrate the research boundary

Open the optional **Start study session** control only when a real pilot has been approved. The interface supports anonymous session IDs, task timing, usefulness feedback, and confidence reflections, but these controls are not evidence by themselves.

For an ordinary software demonstration, leave the study session inactive and say:

> The product workflow is implemented; human-study evidence is intentionally not claimed until the protocol receives appropriate review and participants provide consent.

## Optional reliability demonstration

The default Compose deployment is a single API service. It is suitable for demonstrating retrieval, explanations, and evidence preview.

The distributed research topology is a separate experiment. Run it only when you want to demonstrate controlled failure behaviour:

```powershell
docker compose -f infrastructure/docker/compose.research-distributed.yaml up --build -d
python tools/run_distributed_experiment.py --help
```

Use the experiment documentation to record topology, timeout, retry, circuit, and fault settings. A degraded response must be described as a systems observation, not as evidence of better relevance.

## Screenshot package

The repository does not commit local screenshot captures because browser
captures can contain machine-specific paths, telemetry, or private research
data. Generate the reviewer package locally using the checklist below, then
attach only approved public-safe images to an academic submission.

## Suggested screenshot set

For an academic review, capture these five views at the same browser size:

1. **Landing view** — the NEBULA title, search box, ranking selector, and research note.
2. **Baseline results** — BM25 results for `deployment rollback`, including score and term contributions.
3. **Mode comparison** — the same query under trust-aware ranking with signal chips visible.
4. **Evidence preview** — source text opened from a result.
5. **Failure or limitation** — either an explicit offline state or the study-consent boundary.

Use descriptive filenames such as:

```text
01-search-workspace.png
02-bm25-results.png
03-trust-signals.png
04-source-evidence.png
05-explicit-boundary.png
```

Do not include private paths, telemetry exports, participant information, API tokens, or unreviewed research data in screenshots.

## Demo claims to use

Use language that matches the current evidence:

- “This is a transparent retrieval prototype.”
- “The interface exposes ranking signals and lets the reviewer inspect the source.”
- “The modes are implemented baselines and research variants.”
- “The current fixture is synthetic and is used for regression testing.”
- “The research question is whether these signals improve retrieval on independently judged engineering queries.”

Avoid saying:

- “NEBULA understands the truth.”
- “Trust-aware ranking is proven better.”
- “The current metrics generalize to engineering teams.”
- “The study has been completed.”
- “The system is production-ready.”

## Follow-up links

- [Academic research brief](research/academic-research-brief.md)
- [Research plan](research/research-plan.md)
- [Evaluation guide](../benchmarks/evaluation/README.md)
- [Search workspace README](../apps/search-ui/README.md)
- [Docker deployment guide](../infrastructure/docker/README.md)
