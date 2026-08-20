# Local Development

## Prerequisites

- JDK 17 or newer (`java` and `javac`)
- Python 3 for serving the static search workspace

## Continuous validation

GitHub Actions runs the research export validator and deterministic PMF report generation, then compiles the Java production and test sources and runs the telemetry, research export, and HTTP search integration tests. The workflow is defined in `.github/workflows/validation.yml` and runs on pushes and pull requests.

## Compile the Java backend

From the repository root, compile both dependency-free Java modules into one local build directory:

```powershell
$javaHome = $env:JAVA_HOME
$javac = Join-Path $javaHome 'bin\javac.exe'
$sources = @(Get-ChildItem `
  'services\ingestion\src\main\java', `
  'algorithms\lexical\src\main\java' `
  -Recurse -Filter *.java | ForEach-Object FullName)
New-Item -ItemType Directory -Force -Path 'build\classes' | Out-Null
& $javac -encoding UTF-8 -d 'build\classes' $sources
```

## Start the search API

```powershell
$java = Join-Path $env:JAVA_HOME 'bin\java.exe'
& $java -cp 'build\classes' com.nebula.search.LexicalSearchHttpServer '.\benchmarks\evaluation\corpus-v1'
```

The API listens on `http://127.0.0.1:8082` and loads every Markdown file below the supplied directory at startup. This makes the local search index available after every restart.

To persist pilot search and feedback events across API restarts, pass a third argument for the append-only telemetry log:

```powershell
& $java -cp 'build\classes' com.nebula.search.LexicalSearchHttpServer '.\benchmarks\evaluation\corpus-v1' 8082 '.\data\nebula-telemetry.jsonl'
```

The telemetry file contains local research events only; protect it like other pilot data and do not commit it to Git.

For a traceable study wave, append metadata arguments in this order: study version, corpus version, study wave, query-set version, and code version (usually the Git commit SHA):

```powershell
& $java -cp 'build\classes' com.nebula.search.LexicalSearchHttpServer '.\benchmarks\evaluation\corpus-v1' 8082 '.\data\nebula-telemetry.jsonl' pilot-v1 corpus-v1 wave-01 query-set-v1 abc1234
```

The same values can be supplied through `NEBULA_STUDY_VERSION`, `NEBULA_CORPUS_VERSION`, `NEBULA_STUDY_WAVE`, `NEBULA_QUERY_SET_VERSION`, and `NEBULA_CODE_VERSION` when using the shorter startup command. The manifest exposes these values without exposing the local corpus path.

The search workspace creates one anonymous session ID per browser profile and sends it as `X-Session-Id`. The API records that ID, an epoch timestamp, and the query with each persisted event. No account or personal identity is required for session-level analysis.

The running build describes its research event schema and privacy boundary at `GET http://127.0.0.1:8082/v1/research/manifest`.

The UI sends `X-Research-Consent: true` only after the participant accepts the study dialog. Persistent servers ignore research events without that header; in-memory test servers continue recording for integration-test coverage.

Export the persisted research events for analysis:

```powershell
Invoke-WebRequest 'http://127.0.0.1:8082/v1/research/export?format=csv' -OutFile '.\data\nebula-research.csv'
Invoke-WebRequest 'http://127.0.0.1:8082/v1/research/export?format=json' -OutFile '.\data\nebula-research.json'
```

## Start the search workspace

In a second terminal:

```powershell
python -m http.server 5173 --directory .\apps\search-ui
```

Open `http://127.0.0.1:5173`. The UI connects to the API at port 8082 by default.

## Quick health check

```powershell
Invoke-WebRequest http://127.0.0.1:8082/health/live
```
