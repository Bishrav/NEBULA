package com.nebula.search;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.concurrent.Executors;

/** Research-only HTTP coordinator for the repeatable multi-container experiment. */
public final class ResearchCoordinatorHttpServer {
    private final HttpServer server;
    private final List<HttpShardClient> shards;

    private ResearchCoordinatorHttpServer(int port, List<HttpShardClient> shards) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(bindAddress(), port), 0);
        this.shards = shards;
        server.createContext("/health/live", exchange -> respond(exchange, 200, "{\"status\":\"UP\"}"));
        server.createContext("/v1/search", this::search);
        server.createContext("/v1/metrics", exchange -> respond(exchange, 200, metricsJson()));
        server.setExecutor(Executors.newCachedThreadPool());
    }

    public static ResearchCoordinatorHttpServer createFromEnvironment() throws IOException {
        return new ResearchCoordinatorHttpServer(port(), configuredShards());
    }

    public void start() {
        server.start();
        System.out.println("NEBULA research coordinator listening on " + bindAddress() + ":" + server.getAddress().getPort());
    }

    private void search(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            respond(exchange, 405, "{\"error\":\"method not allowed\"}");
            return;
        }
        Map<String, String> parameters = queryParameters(exchange.getRequestURI().getRawQuery());
        String query = parameters.get("q");
        if (query == null || query.trim().isEmpty()) {
            respond(exchange, 400, "{\"error\":\"q is required\"}");
            return;
        }
        int limit;
        try {
            limit = Integer.parseInt(parameters.getOrDefault("limit", "10"));
            if (limit < 1 || limit > 100) throw new NumberFormatException();
        } catch (NumberFormatException invalid) {
            respond(exchange, 400, "{\"error\":\"limit must be between 1 and 100\"}");
            return;
        }
        long started = System.nanoTime();
        NetworkSearchResponse response = new NetworkQueryCoordinator(shards).search(query, limit);
        respond(exchange, 200, responseJson(response, System.nanoTime() - started));
    }

    private String responseJson(NetworkSearchResponse response, long latencyNanos) {
        StringBuilder json = new StringBuilder("{\"partial\":").append(response.isPartial())
                .append(",\"queriedShards\":").append(strings(response.getQueriedShards()))
                .append(",\"failedShards\":").append(strings(response.getFailedShards()))
                .append(",\"latencyNanos\":").append(latencyNanos)
                .append(",\"results\":[");
        for (int i = 0; i < response.getResults().size(); i++) {
            if (i > 0) json.append(',');
            SearchResult result = response.getResults().get(i);
            json.append("{\"documentId\":\"").append(escape(result.getDocument().getDocumentId()))
                    .append("\",\"sourcePath\":\"").append(escape(result.getDocument().getSourcePath()))
                    .append("\",\"score\":").append(result.getScore()).append('}');
        }
        json.append("],\"metrics\":").append(metricsJson()).append('}');
        return json.toString();
    }

    private String metricsJson() {
        long attempts = 0, failures = 0, successes = 0;
        int open = 0;
        for (HttpShardClient shard : shards) {
            ShardHealth.Snapshot snapshot = shard.health().snapshot();
            attempts += snapshot.getTotalAttempts();
            failures += snapshot.getTotalFailures();
            successes += snapshot.getTotalSuccesses();
            if ("OPEN".equals(snapshot.getState())) open++;
        }
        return "{\"requestAdmissions\":" + attempts + ",\"retryAttempts\":" + failures
                + ",\"successfulShardRequests\":" + successes + ",\"openCircuits\":" + open + "}";
    }

    private static List<HttpShardClient> configuredShards() {
        String raw = System.getenv("NEBULA_SHARDS");
        if (raw == null || raw.trim().isEmpty()) throw new IllegalArgumentException("NEBULA_SHARDS is required");
        List<HttpShardClient> clients = new ArrayList<>();
        for (String entry : raw.split(",")) {
            String[] pair = entry.trim().split("=", 2);
            if (pair.length != 2) throw new IllegalArgumentException("invalid shard entry: " + entry);
            clients.add(new HttpShardClient(pair[0].trim(), pair[1].trim(), envInt("NEBULA_SHARD_TIMEOUT_MS", 500),
                    envInt("NEBULA_SHARD_MAX_ATTEMPTS", 2), System.getenv("NEBULA_API_TOKEN"),
                    envInt("NEBULA_CIRCUIT_FAILURE_THRESHOLD", 3), envLong("NEBULA_CIRCUIT_COOLDOWN_MS", 1000L)));
        }
        return clients;
    }

    private static Map<String, String> queryParameters(String raw) {
        Map<String, String> parameters = new LinkedHashMap<>();
        if (raw == null) return parameters;
        for (String part : raw.split("&")) {
            String[] pair = part.split("=", 2);
            if (pair.length == 2) parameters.put(URLDecoder.decode(pair[0], StandardCharsets.UTF_8), URLDecoder.decode(pair[1], StandardCharsets.UTF_8));
        }
        return parameters;
    }

    private static String strings(List<String> values) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) { if (i > 0) json.append(','); json.append('"').append(escape(values.get(i))).append('"'); }
        return json.append(']').toString();
    }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (java.io.OutputStream output = exchange.getResponseBody()) { output.write(bytes); }
    }

    private static String escape(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n"); }
    private static String bindAddress() { return System.getenv().getOrDefault("NEBULA_BIND_ADDRESS", "0.0.0.0"); }
    private static int port() { return envInt("NEBULA_COORDINATOR_PORT", 8082); }
    private static int envInt(String name, int fallback) { try { int value = Integer.parseInt(System.getenv(name)); return value > 0 ? value : fallback; } catch (Exception ignored) { return fallback; } }
    private static long envLong(String name, long fallback) { try { long value = Long.parseLong(System.getenv(name)); return value > 0 ? value : fallback; } catch (Exception ignored) { return fallback; } }

    public static void main(String[] args) throws Exception { createFromEnvironment().start(); }
}
