# NEBULA container deployment

This deployment runs the Java search API with the versioned evaluation corpus and stores telemetry in a named Docker volume. It is a production-like pilot baseline, not a production security certification.

From the repository root:

```powershell
docker compose -f infrastructure/docker/compose.yaml up --build
```

Verify the container before a pilot:

```powershell
Invoke-WebRequest http://127.0.0.1:8082/health/ready
python tools/pilot_preflight.py --base-url http://127.0.0.1:8082 --study-wave deployment-check --corpus-version corpus-v1 --query-set-version query-set-v1 --code-version container --output .\data\container-preflight.json
```

Compose sets `NEBULA_BIND_ADDRESS=0.0.0.0` so the published container port is
reachable from the host. Direct JVM runs keep the safer loopback default unless
the bind address is explicitly overridden.

Set `NEBULA_ALLOWED_ORIGIN` to the exact browser origin before exposing the API beyond local development. The default local UI origin is `http://127.0.0.1:5174`. Keep the telemetry volume restricted to the research team, export it through the approved workflow, and never commit its contents.

The container currently ships the public synthetic evaluation corpus. Private document ingestion, authentication, authorization, TLS termination, backups, and operational alerting remain deployment requirements before production use.

## Research distributed topology

The production-like developer Compose file intentionally remains a single API service. For the secondary distributed experiment, use compose.research-distributed.yaml; it starts one research coordinator and three primary/replica shard pairs. The topology is for repeatable experiments, not a production capacity claim.

The coordinator is ResearchCoordinatorHttpServer. It exposes /v1/search, returning explicit partial, failedShards, latency, result IDs, and aggregate shard-health counters. `tools/run_distributed_experiment.py` converts repeated observations into JSON, CSV, and Markdown artifacts. When supplied with a healthy baseline and qrels, it reports healthy NDCG, degraded NDCG, and `ndcgDegradation = healthy - degraded`; without those inputs it reports the metric as not measured.

Shard fault controls are inactive unless explicitly set:

- NEBULA_FAULT_MODE=latency with NEBULA_FAULT_DELAY_MS
- NEBULA_FAULT_MODE=http500
- NEBULA_FAULT_EVERY to make a deterministic periodic fault

Stopping a named Compose service is reserved for unavailable-endpoint scenarios. Record the exact Compose command, topology, hardware, timeout, retry, and circuit settings in the experiment manifest.

For internal shard traffic, set `NEBULA_API_TOKEN` on the API container and configure the coordinator with the same secret through its deployment secret manager. Do not place the token in Compose files, source code, or committed `.env` files.

Monitor coordinator-side `ShardHealth` snapshots for open circuits, retry exhaustion, and recovered probes. The current implementation is an in-process monitoring baseline; export these values to the deployment metrics system before production.
