CREATE TABLE IF NOT EXISTS documents (
    document_id       CHAR(64) PRIMARY KEY,
    source_path       TEXT NOT NULL,
    source_type       VARCHAR(32) NOT NULL,
    title             TEXT NOT NULL,
    normalized_text   TEXT NOT NULL,
    content_hash      CHAR(64) NOT NULL,
    current_version   INTEGER NOT NULL CHECK (current_version > 0),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (source_path, content_hash)
);

CREATE INDEX IF NOT EXISTS idx_documents_source_path
    ON documents (source_path);

CREATE INDEX IF NOT EXISTS idx_documents_content_hash
    ON documents (content_hash);

CREATE TABLE IF NOT EXISTS ingestion_jobs (
    job_id            UUID PRIMARY KEY,
    source_path       TEXT NOT NULL,
    status            VARCHAR(16) NOT NULL,
    document_id       CHAR(64),
    document_version  INTEGER,
    error_message     TEXT,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at      TIMESTAMPTZ,
    CHECK (status IN ('QUEUED', 'PROCESSING', 'COMPLETED', 'FAILED'))
);

CREATE INDEX IF NOT EXISTS idx_ingestion_jobs_source_path
    ON ingestion_jobs (source_path);
