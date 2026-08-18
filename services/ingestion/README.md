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

## Local validation without Maven

From the repository root:

```powershell
New-Item -ItemType Directory -Force .\services\ingestion\out | Out-Null
javac -d .\services\ingestion\out .\services\ingestion\src\main\java\com\nebula\ingestion\*.java .\services\ingestion\src\test\java\com\nebula\ingestion\*.java
java -cp .\services\ingestion\out com.nebula.ingestion.DocumentIngestorTest
```

Maven support is included in `pom.xml` for CI once Maven is available.
