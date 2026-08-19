package com.nebula.search;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Append-only local telemetry log that replays search and feedback events on startup. */
public final class PersistentTelemetryStore {
    private static final Pattern FIELD = Pattern.compile("\\\"([^\\\"]+)\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"])*)\\\"");
    private static final Pattern NUMBER = Pattern.compile("\\\"([^\\\"]+)\\\"\\s*:\\s*(-?\\d+)");
    private static final Pattern BOOLEAN = Pattern.compile("\\\"([^\\\"]+)\\\"\\s*:\\s*(true|false)");

    private final Path file;
    private final FeedbackStore feedbackStore = new FeedbackStore();
    private final SearchMetrics searchMetrics = new SearchMetrics();

    private PersistentTelemetryStore(Path file) throws IOException {
        this.file = file;
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) Files.createDirectories(parent);
        if (Files.exists(file)) replay(Files.readAllLines(file, StandardCharsets.UTF_8));
    }

    public static PersistentTelemetryStore open(Path file) throws IOException {
        if (file == null) throw new IllegalArgumentException("telemetry file must not be null");
        return new PersistentTelemetryStore(file);
    }

    public synchronized void recordSearch(String mode, int resultCount, long latencyNanos) throws IOException {
        searchMetrics.record(mode, resultCount, latencyNanos);
        append("{\"type\":\"search\",\"mode\":\"" + escape(mode)
                + "\",\"results\":" + resultCount + ",\"latencyNanos\":" + Math.max(0L, latencyNanos) + "}");
    }

    public synchronized void recordFeedback(FeedbackRecord feedback) throws IOException {
        feedbackStore.record(feedback);
        append("{\"type\":\"feedback\",\"query\":\"" + escape(feedback.getQuery())
                + "\",\"mode\":\"" + escape(feedback.getMode())
                + "\",\"documentId\":\"" + escape(feedback.getDocumentId())
                + "\",\"sourcePath\":\"" + escape(feedback.getSourcePath())
                + "\",\"useful\":" + feedback.isUseful() + "}");
    }

    public FeedbackStore feedbackStore() { return feedbackStore; }
    public SearchMetrics searchMetrics() { return searchMetrics; }
    public Path file() { return file; }

    private void replay(List<String> lines) {
        for (String line : lines) {
            try {
                String type = stringField(line, "type");
                if ("search".equals(type)) {
                    searchMetrics.record(stringField(line, "mode"), (int) numberField(line, "results"), numberField(line, "latencyNanos"));
                } else if ("feedback".equals(type)) {
                    feedbackStore.record(new FeedbackRecord(stringField(line, "query"), stringField(line, "mode"),
                            stringField(line, "documentId"), stringField(line, "sourcePath"), booleanField(line, "useful")));
                }
            } catch (RuntimeException ignored) {
                // Ignore a truncated final event so a process interruption cannot prevent startup.
            }
        }
    }

    private void append(String event) throws IOException {
        Files.write(file, (event + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
    }

    private static String stringField(String json, String field) {
        Matcher matcher = FIELD.matcher(json);
        while (matcher.find()) if (field.equals(matcher.group(1))) return unescape(matcher.group(2));
        throw new IllegalArgumentException("missing field: " + field);
    }

    private static long numberField(String json, String field) {
        Matcher matcher = NUMBER.matcher(json);
        while (matcher.find()) if (field.equals(matcher.group(1))) return Long.parseLong(matcher.group(2));
        throw new IllegalArgumentException("missing field: " + field);
    }

    private static boolean booleanField(String json, String field) {
        Matcher matcher = BOOLEAN.matcher(json);
        while (matcher.find()) if (field.equals(matcher.group(1))) return Boolean.parseBoolean(matcher.group(2));
        throw new IllegalArgumentException("missing field: " + field);
    }

    private static String escape(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "\\r").replace("\n", "\\n"); }
    private static String unescape(String value) { return value.replace("\\\"", "\"").replace("\\\\", "\\").replace("\\r", "\r").replace("\\n", "\n"); }
}
