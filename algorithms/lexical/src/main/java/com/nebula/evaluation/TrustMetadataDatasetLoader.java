package com.nebula.evaluation;

import com.nebula.search.DocumentTrustMetadata;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Loads versioned source-authority and verification-age metadata. */
public final class TrustMetadataDatasetLoader {
    private static final long MILLIS_PER_DAY = 86_400_000L;

    public List<DocumentTrustMetadata> load(Path source, long nowEpochMillis) throws IOException {
        List<DocumentTrustMetadata> metadata = new ArrayList<>();
        for (String line : Files.readAllLines(source, StandardCharsets.UTF_8)) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("source_path|")) continue;
            String[] columns = trimmed.split("\\|", 5);
            if (columns.length != 5) throw new IOException("invalid trust metadata row: " + line);
            try {
                double authority = Double.parseDouble(columns[1]);
                long daysAgo = Long.parseLong(columns[2]);
                metadata.add(new DocumentTrustMetadata(columns[0], authority,
                        nowEpochMillis - daysAgo * MILLIS_PER_DAY, columns[3], columns[4]));
            } catch (NumberFormatException exception) {
                throw new IOException("invalid trust metadata values: " + line, exception);
            }
        }
        return metadata;
    }
}
