# NEBULA Search Workspace

This is the first user-facing product slice for NEBULA. It is a dependency-free static interface for testing the core search loop with engineering users.

## Run locally

Start the Java search API on port `8082`, then serve this directory from a local HTTP server:

```powershell
python -m http.server 5173 --directory .\apps\search-ui
```

Open `http://127.0.0.1:5173`. The API endpoint can be changed in the interface when the service runs elsewhere.

## Product hypothesis

Engineering users will trust and prefer NEBULA when they can find a relevant source quickly and understand why it ranked. The interface intentionally shows ranking signals instead of presenting a black-box answer.

## Current limitations

- Results can open the indexed source text in an evidence preview for verification.
- One-character technical term corrections are shown in the ranking explanations when the index has a close vocabulary match.
- The API must be running separately.
- Authentication, permissions, filters, feedback capture, and document previews are future milestones.
