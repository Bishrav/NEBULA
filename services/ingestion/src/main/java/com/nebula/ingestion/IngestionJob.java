package com.nebula.ingestion;

/** Immutable view of an ingestion attempt. */
public final class IngestionJob {
    private final String jobId;
    private final String sourcePath;
    private final IngestionStatus status;
    private final String documentId;
    private final int version;
    private final String error;

    private IngestionJob(String jobId, String sourcePath, IngestionStatus status,
                         String documentId, int version, String error) {
        this.jobId = jobId;
        this.sourcePath = sourcePath;
        this.status = status;
        this.documentId = documentId;
        this.version = version;
        this.error = error;
    }

    public static IngestionJob completed(String jobId, DocumentRecord document, int version) {
        return new IngestionJob(jobId, document.getSourcePath(), IngestionStatus.COMPLETED,
                document.getDocumentId(), version, null);
    }

    public static IngestionJob failed(String jobId, String sourcePath, String error) {
        return new IngestionJob(jobId, sourcePath, IngestionStatus.FAILED, null, 0, error);
    }

    public String getJobId() { return jobId; }
    public String getSourcePath() { return sourcePath; }
    public IngestionStatus getStatus() { return status; }
    public String getDocumentId() { return documentId; }
    public int getVersion() { return version; }
    public String getError() { return error; }
}
