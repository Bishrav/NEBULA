package com.nebula.search;

/** Explicit trust metadata kept separate from document content and lexical ranking. */
public final class DocumentTrustMetadata {
    private final String sourcePath;
    private final double authority;
    private final long lastVerifiedEpochMillis;
    private final String owner;
    private final String verificationStatus;

    public DocumentTrustMetadata(String sourcePath, double authority, long lastVerifiedEpochMillis,
                                 String owner, String verificationStatus) {
        if (sourcePath == null || sourcePath.trim().isEmpty()) throw new IllegalArgumentException("sourcePath is required");
        if (authority < 0.0 || authority > 1.0) throw new IllegalArgumentException("authority must be between zero and one");
        this.sourcePath = sourcePath;
        this.authority = authority;
        this.lastVerifiedEpochMillis = lastVerifiedEpochMillis;
        this.owner = owner == null ? "unknown" : owner;
        this.verificationStatus = verificationStatus == null ? "unverified" : verificationStatus;
    }

    public String getSourcePath() { return sourcePath; }
    public double getAuthority() { return authority; }
    public long getLastVerifiedEpochMillis() { return lastVerifiedEpochMillis; }
    public String getOwner() { return owner; }
    public String getVerificationStatus() { return verificationStatus; }
}
