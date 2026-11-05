param(
    [int]$ApiPort = 8082,
    [int]$UiPort = 5174
)

$ErrorActionPreference = 'Stop'
$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$javaHome = $env:JAVA_HOME
if ([string]::IsNullOrWhiteSpace($javaHome)) {
    throw 'JAVA_HOME is not set. Install JDK 17+ and set JAVA_HOME before starting the demo.'
}

$javac = Join-Path $javaHome 'bin\javac.exe'
$java = Join-Path $javaHome 'bin\java.exe'
if (-not (Test-Path $javac) -or -not (Test-Path $java)) {
    throw "JDK executables were not found below JAVA_HOME: $javaHome"
}

$python = (Get-Command python -ErrorAction SilentlyContinue).Source
if ([string]::IsNullOrWhiteSpace($python)) {
    throw 'Python 3 was not found on PATH. It is required to serve the static search workspace.'
}

$buildDir = Join-Path $repoRoot 'build\classes'
$sourceFiles = @(
    Get-ChildItem (Join-Path $repoRoot 'services\ingestion\src\main\java'),
        (Join-Path $repoRoot 'algorithms\lexical\src\main\java') -Recurse -Filter '*.java' |
        ForEach-Object FullName
)
New-Item -ItemType Directory -Force -Path $buildDir | Out-Null
& $javac -encoding UTF-8 -d $buildDir $sourceFiles
if ($LASTEXITCODE -ne 0) {
    throw 'Java compilation failed.'
}

$corpusDir = Join-Path $repoRoot 'datasets\research-corpus-v1\normalized'
$embeddingCache = Join-Path $repoRoot 'data\review-batch-01-annotation-private\modern-embedding-cache-bge-base-en-v1.5.tsv'
if (-not (Test-Path $corpusDir)) {
    throw "Research corpus directory not found: $corpusDir"
}
if (-not (Test-Path $embeddingCache)) {
    Write-Warning "Private BGE cache not found. The API will still start, but bge-hybrid will be unavailable."
}

function Test-PortInUse([int]$Port) {
    return $null -ne (Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue)
}

$env:NEBULA_EMBEDDING_CACHE = $embeddingCache
$apiUrl = "http://127.0.0.1:$ApiPort"
$uiUrl = "http://127.0.0.1:$UiPort"

if (-not (Test-PortInUse $ApiPort)) {
    Start-Process -FilePath $java -WorkingDirectory $repoRoot -ArgumentList @(
        '-cp', $buildDir,
        'com.nebula.search.LexicalSearchHttpServer',
        $corpusDir,
        $ApiPort
    ) | Out-Null
    Write-Host "Started NEBULA API on $apiUrl"
} else {
    Write-Host "Reusing the existing API on $apiUrl"
}

if (-not (Test-PortInUse $UiPort)) {
    Start-Process -FilePath $python -WorkingDirectory $repoRoot -ArgumentList @(
        '-m', 'http.server', $UiPort, '--directory', (Join-Path $repoRoot 'apps\search-ui')
    ) | Out-Null
    Write-Host "Started search workspace on $uiUrl"
} else {
    Write-Host "Reusing the existing search workspace on $uiUrl"
}

for ($attempt = 1; $attempt -le 30; $attempt++) {
    try {
        $ready = Invoke-WebRequest -UseBasicParsing "$apiUrl/health/ready"
        if ($ready.StatusCode -eq 200) {
            Write-Host "API ready: $($ready.Content)"
            Write-Host "Open the demo: $uiUrl"
            exit 0
        }
    } catch {
        Start-Sleep -Milliseconds 500
    }
}

throw "The API did not become ready at $apiUrl/health/ready"
