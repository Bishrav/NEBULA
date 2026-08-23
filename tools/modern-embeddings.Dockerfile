FROM python:3.11-slim

WORKDIR /workspace
COPY tools/modern-embeddings-requirements.txt /workspace/tools/modern-embeddings-requirements.txt
RUN python -m pip install --no-cache-dir numpy==1.26.4 \
    && python -m pip install --no-cache-dir torch==2.13.0+cpu --index-url https://download.pytorch.org/whl/cpu \
    && python -m pip install --no-cache-dir sentence-transformers==6.0.0

COPY tools/generate_modern_embeddings.py /workspace/tools/generate_modern_embeddings.py
ENTRYPOINT ["python", "/workspace/tools/generate_modern_embeddings.py"]
