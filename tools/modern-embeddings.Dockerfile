FROM python:3.11-slim

WORKDIR /workspace
COPY tools/modern-embeddings-requirements.txt /workspace/tools/modern-embeddings-requirements.txt
RUN python -m pip install --no-cache-dir -r /workspace/tools/modern-embeddings-requirements.txt

COPY tools/generate_modern_embeddings.py /workspace/tools/generate_modern_embeddings.py
ENTRYPOINT ["python", "/workspace/tools/generate_modern_embeddings.py"]
