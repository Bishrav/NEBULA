# Local Development

## Prerequisites

- JDK 17 or newer (`java` and `javac`)
- Python 3 for serving the static search workspace

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
& $java -cp 'build\classes' com.nebula.search.LexicalSearchHttpServer
```

The API listens on `http://127.0.0.1:8082`.

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
