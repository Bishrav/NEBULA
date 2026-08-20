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

    public synchronized void recordSearch(String sessionId, String query, String mode, int resultCount, long latencyNanos) throws IOException {
        searchMetrics.record(mode, resultCount, latencyNanos);
        append("{\"type\":\"search\",\"sessionId\":\"" + escape(sessionId)
                + "\",\"timestamp\":" + System.currentTimeMillis()
                + ",\"query\":\"" + escape(query) + "\",\"mode\":\"" + escape(mode)
                + "\",\"results\":" + resultCount + ",\"latencyNanos\":" + Math.max(0L, latencyNanos) + "}");
    }

    public synchronized void recordFeedback(String sessionId, FeedbackRecord feedback) throws IOException {
        feedbackStore.record(feedback);
        append("{\"type\":\"feedback\",\"sessionId\":\"" + escape(sessionId)
                + "\",\"timestamp\":" + System.currentTimeMillis()
                + ",\"query\":\"" + escape(feedback.getQuery())
                + "\",\"mode\":\"" + escape(feedback.getMode())
                + "\",\"documentId\":\"" + escape(feedback.getDocumentId())
                + "\",\"sourcePath\":\"" + escape(feedback.getSourcePath())
                + "\",\"useful\":" + feedback.isUseful() + "}");
    }

    public synchronized void recordTask(String sessionId, String taskId, String action, boolean success, long durationMs) throws IOException {
        append("{\"type\":\"task\",\"sessionId\":\"" + escape(sessionId)
                + "\",\"timestamp\":" + System.currentTimeMillis()
                + ",\"taskId\":\"" + escape(taskId) + "\",\"action\":\"" + escape(action)
                + "\",\"durationMs\":" + Math.max(0L, durationMs)
                + ",\"success\":" + success + "}");
    }

    public FeedbackStore feedbackStore() { return feedbackStore; }
    public SearchMetrics searchMetrics() { return searchMetrics; }
    public Path file() { return file; }

    public synchronized String exportJson() throws IOException {
        List<String> lines = Files.exists(file) ? Files.readAllLines(file, StandardCharsets.UTF_8) : List.of();
        StringBuilder body = new StringBuilder("[");
        int count = 0;
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            if (count++ > 0) body.append(',');
            body.append(line.trim());
        }
        return body.append(']').toString();
    }

    public synchronized String exportCsv() throws IOException {
        List<String> lines = Files.exists(file) ? Files.readAllLines(file, StandardCharsets.UTF_8) : List.of();
        StringBuilder body = new StringBuilder("type,sessionId,timestamp,query,mode,results,latencyNanos,documentId,sourcePath,useful,taskId,action,durationMs,success\n");
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            body.append(csv(stringField(line, "type"))).append(',')
                    .append(csv(optionalString(line, "sessionId"))).append(',')
                    .append(optionalNumber(line, "timestamp")).append(',')
                    .append(csv(optionalString(line, "query"))).append(',')
                    .append(csv(optionalString(line, "mode"))).append(',')
                    .append(optionalNumber(line, "results")).append(',')
                    .append(optionalNumber(line, "latencyNanos")).append(',')
                    .append(csv(optionalString(line, "documentId"))).append(',')
                    .append(csv(optionalString(line, "sourcePath"))).append(',')
                    .append(optionalBoolean(line, "useful")).append(',')
                    .append(csv(optionalString(line, "taskId"))).append(',')
                    .append(csv(optionalString(line, "action"))).append(',')
                    .append(optionalNumber(line, "durationMs")).append(',')
                    .append(optionalBoolean(line, "success")).append('\n');
        }
        return body.toString();
    }

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

    private static String optionalString(String json, String field) {
        try { return stringField(json, field); } catch (IllegalArgumentException ignored) { return ""; }
    }

    private static long numberField(String json, String field) {
        Matcher matcher = NUMBER.matcher(json);
        while (matcher.find()) if (field.equals(matcher.group(1))) return Long.parseLong(matcher.group(2));
        throw new IllegalArgumentException("missing field: " + field);
    }

    private static String optionalNumber(String json, String field) {
        try { return Long.toString(numberField(json, field)); } catch (IllegalArgumentException ignored) { return ""; }
    }

    private static boolean booleanField(String json, String field) {
        Matcher matcher = BOOLEAN.matcher(json);
        while (matcher.find()) if (field.equals(matcher.group(1))) return Boolean.parseBoolean(matcher.group(2));
        throw new IllegalArgumentException("missing field: " + field);
    }

    private static String optionalBoolean(String json, String field) {
        try { return Boolean.toString(booleanField(json, field)); } catch (IllegalArgumentException ignored) { return ""; }
    }

    private static String csv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private static String escape(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "\\r").replace("\n", "\\n"); }
    private static String unescape(String value) { return value.replace("\\\"", "\"").replace("\\\\", "\\").replace("\\r", "\r").replace("\\n", "\n"); }
}
