# NEBULA Ingestion Core

The ingestion core converts Markdown files into deterministic `DocumentRecord` values for indexing.

## Current behavior

- Accepts `.md` and `.markdown` files
- Reads UTF-8 content
- Extracts the first level-one heading as the title
- Falls back to the filename when no H1 exists
- Removes common Markdown presentation syntax
- Preserves readable link labels
- Computes a SHA-256 normalized content hash
- Computes a deterministic document ID from source type, canonical path, and content hash
- Tracks idempotent ingestion jobs and monotonic source versions
- Exposes a local HTTP API for ingestion, job status, document retrieval, and health checks
- Includes a PostgreSQL migration for document and ingestion-job metadata

## Local validation without Maven

From the repository root:

```powershell
New-Item -ItemType Directory -Force .\services\ingestion\out | Out-Null
javac -d .\services\ingestion\out .\services\ingestion\src\main\java\com\nebula\ingestion\*.java .\services\ingestion\src\test\java\com\nebula\ingestion\*.java
java -cp .\services\ingestion\out com.nebula.ingestion.DocumentIngestorTest
java -cp .\services\ingestion\out com.nebula.ingestion.IngestionServiceTest
java -cp .\services\ingestion\out com.nebula.ingestion.IngestionHttpServerTest
```

Maven support is included in `pom.xml` for CI once Maven is available.

## HTTP API

Start the local server with:

```powershell
java -cp .\services\ingestion\out com.nebula.ingestion.IngestionHttpServer
```

Submit a Markdown document:

```powershell
Invoke-WebRequest `
  -Method Post `
  -Uri http://127.0.0.1:8081/v1/documents `
  -Headers @{ 'X-Source-Path' = 'docs/runbook.md' } `
  -ContentType 'text/markdown' `
  -Body '# Runbook`n`nRestart the service.'
```

Endpoints:

- `GET /health/live`
- `GET /health/ready`
- `POST /v1/documents`
- `GET /v1/jobs/{jobId}`
- `GET /v1/documents/{documentId}`
