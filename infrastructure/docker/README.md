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

Set `NEBULA_ALLOWED_ORIGIN` to the exact browser origin before exposing the API beyond local development. Keep the telemetry volume restricted to the research team, export it through the approved workflow, and never commit its contents.

The container currently ships the public synthetic evaluation corpus. Private document ingestion, authentication, authorization, TLS termination, backups, and operational alerting remain deployment requirements before production use.

For internal shard traffic, set `NEBULA_API_TOKEN` on the API container and configure the coordinator with the same secret through its deployment secret manager. Do not place the token in Compose files, source code, or committed `.env` files.
