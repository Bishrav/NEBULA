# NEBULA research notebooks

`nebula_pmf_analysis.ipynb` is the first reproducible analysis artifact for the pilot. It loads the CSV export from the research endpoint, validates the event schema, calculates PMF-oriented measures, and produces ranking-mode and feedback charts.

## Run with a real pilot export

Start the API with persistent telemetry, export the events, then run the notebook from the repository root:

```powershell
Invoke-WebRequest 'http://127.0.0.1:8082/v1/research/export?format=csv' -OutFile '.\data\nebula-research.csv'
python -m nbconvert --execute --to notebook --inplace .\notebooks\nebula_pmf_analysis.ipynb
```

If `data/nebula-research.csv` is absent, the notebook uses `data/nebula-research.sample.csv`, which is synthetic and must not be used as university evidence.
