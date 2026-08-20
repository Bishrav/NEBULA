package com.nebula.evaluation;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Tests trust metadata fixture parsing and deterministic age conversion. */
public final class TrustMetadataDatasetLoaderTest {
    public static void main(String[] args) throws Exception {
        Path file = Files.createTempFile("nebula-trust-", ".psv");
        try {
            Files.write(file, ("source_path|authority|days_since_verification|owner|status\n"
                    + "docs/runbook.md|0.8|2|release|verified\n").getBytes(StandardCharsets.UTF_8));
            long now = 1_700_000_000_000L;
            java.util.List<com.nebula.search.DocumentTrustMetadata> metadata =
                    new TrustMetadataDatasetLoader().load(file, now);
            check(metadata.size() == 1, "one metadata row is parsed");
            check(metadata.get(0).getAuthority() == 0.8, "authority is parsed");
            check(metadata.get(0).getLastVerifiedEpochMillis() == now - 2L * 86_400_000L, "age is deterministic");
            System.out.println("TrustMetadataDatasetLoaderTest: PASS");
        } finally {
            Files.deleteIfExists(file);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
