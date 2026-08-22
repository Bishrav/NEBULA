package com.nebula.search;

import com.nebula.ingestion.DocumentRecord;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Dependency-free HTTP client for a NEBULA shard search endpoint. */
public final class HttpShardClient {
    private static final Pattern RESULT = Pattern.compile("\\{\\\"documentId\\\":\\\"((?:\\\\.|[^\\\"])*)\\\",\\\"title\\\":\\\"((?:\\\\.|[^\\\"])*)\\\",\\\"sourcePath\\\":\\\"((?:\\\\.|[^\\\"])*)\\\",\\\"score\\\":([-+0-9.eE]+)");
    private final String shardId;
    private final String baseUrl;
    private final int timeoutMillis;
    private final int maxAttempts;
    private final String apiToken;
    private final ShardHealth health;
    private final ShardFaultInjector faultInjector;

    public HttpShardClient(String shardId, String baseUrl, int timeoutMillis, int maxAttempts) {
        this(shardId, baseUrl, timeoutMillis, maxAttempts, null);
    }
    public HttpShardClient(String shardId, String baseUrl, int timeoutMillis, int maxAttempts, String apiToken) {
        this(shardId, baseUrl, timeoutMillis, maxAttempts, apiToken, 3, 5000L);
    }
    public HttpShardClient(String shardId, String baseUrl, int timeoutMillis, int maxAttempts, String apiToken,
                           int failureThreshold, long cooldownMillis) {
        this(shardId, baseUrl, timeoutMillis, maxAttempts, apiToken, failureThreshold, cooldownMillis, ShardFaultInjector.none());
    }
    public HttpShardClient(String shardId, String baseUrl, int timeoutMillis, int maxAttempts, String apiToken,
                           int failureThreshold, long cooldownMillis, ShardFaultInjector faultInjector) {
        if (shardId == null || shardId.trim().isEmpty() || baseUrl == null || baseUrl.trim().isEmpty()) throw new IllegalArgumentException("shard identity and URL are required");
        if (timeoutMillis <= 0 || maxAttempts <= 0) throw new IllegalArgumentException("timeouts and attempts must be positive");
        if (faultInjector == null) throw new IllegalArgumentException("fault injector is required");
        this.shardId = shardId; this.baseUrl = baseUrl.replaceAll("/+$", "");
        this.timeoutMillis = timeoutMillis; this.maxAttempts = maxAttempts; this.apiToken = apiToken; this.faultInjector = faultInjector;
        this.health = new ShardHealth(failureThreshold, cooldownMillis);
    }
    public String getShardId() { return shardId; }
    public ShardHealth health() { return health; }
    public List<SearchResult> search(String query, int limit) throws Exception {
        if (!health.allowRequest()) throw new IllegalStateException("circuit open for shard: " + shardId);
        Exception last = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try { faultInjector.beforeAttempt(shardId, attempt); List<SearchResult> result = request(query, limit); health.recordSuccess(); return result; }
            catch (Exception failure) { last = failure; health.recordFailure(); }
        }
        throw last == null ? new IllegalStateException("request failed") : last;
    }
    private List<SearchResult> request(String query, int limit) throws Exception {
        String target = baseUrl + "/v1/search?q=" + URLEncoder.encode(query, "UTF-8") + "&limit=" + limit;
        HttpURLConnection connection = (HttpURLConnection) new URL(target).openConnection();
        connection.setConnectTimeout(timeoutMillis); connection.setReadTimeout(timeoutMillis); connection.setRequestMethod("GET");
        if (apiToken != null && !apiToken.trim().isEmpty()) connection.setRequestProperty("Authorization", "Bearer " + apiToken);
        int status = connection.getResponseCode();
        InputStream input = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
        String body = read(input);
        if (status != 200) throw new IllegalStateException("shard " + shardId + " returned HTTP " + status);
        return parseResults(body);
    }
    private static List<SearchResult> parseResults(String body) {
        List<SearchResult> results = new ArrayList<>(); Matcher matcher = RESULT.matcher(body);
        while (matcher.find()) {
            String id = unescape(matcher.group(1)); String title = unescape(matcher.group(2)); String path = unescape(matcher.group(3));
            double score = Double.parseDouble(matcher.group(4));
            DocumentRecord document = new DocumentRecord(id, path, "remote", title, title, id);
            results.add(new SearchResult(document, score, new LinkedHashMap<String, Double>()));
        }
        return Collections.unmodifiableList(results);
    }
    private static String read(InputStream input) throws Exception {
        if (input == null) return "";
        try (InputStream stream = input; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024]; int count;
            while ((count = stream.read(buffer)) != -1) output.write(buffer, 0, count);
            return new String(output.toByteArray(), StandardCharsets.UTF_8);
        }
    }
    private static String unescape(String value) { return value.replace("\\\\", "\\").replace("\\\"", "\"").replace("\\n", "\n"); }
}
