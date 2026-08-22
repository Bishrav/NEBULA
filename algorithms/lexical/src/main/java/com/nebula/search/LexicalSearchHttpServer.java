package com.nebula.search;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Local HTTP search service for the lexical retrieval milestone. */
public final class LexicalSearchHttpServer {
    private static final AtomicLong FAULT_REQUESTS = new AtomicLong();
    private final HttpServer server;
    private final SearchCatalog catalog;
    private final FeedbackStore feedbackStore;
    private final SearchMetrics searchMetrics;
    private final PersistentTelemetryStore telemetryStore;
    private final ResearchStudyMetadata studyMetadata;
    private final String allowedOrigin;
    private final String apiToken;
    private ExecutorService executor;

    private LexicalSearchHttpServer(HttpServer server, SearchCatalog catalog, PersistentTelemetryStore telemetryStore,
                                    ResearchStudyMetadata studyMetadata) {
        this.server = server;
        this.catalog = catalog;
        this.telemetryStore = telemetryStore;
        this.studyMetadata = studyMetadata;
        this.allowedOrigin = configuredAllowedOrigin();
        this.apiToken = configuredApiToken();
        this.feedbackStore = telemetryStore == null ? new FeedbackStore() : telemetryStore.feedbackStore();
        this.searchMetrics = telemetryStore == null ? new SearchMetrics() : telemetryStore.searchMetrics();
        server.createContext("/health/live", cors(exchange -> respond(exchange, 200, "{\"status\":\"UP\"}")));
        server.createContext("/health/ready", cors(exchange -> ready(exchange)));
        server.createContext("/v1/index/documents", cors(new IndexHandler()));
        server.createContext("/v1/search", cors(new SearchHandler()));
        server.createContext("/v1/documents", cors(new DocumentHandler()));
        server.createContext("/v1/suggest", cors(new SuggestHandler()));
        server.createContext("/v1/feedback", cors(new FeedbackHandler()));
        server.createContext("/v1/research/tasks", cors(exchange -> researchTask(exchange)));
        server.createContext("/v1/research/observations", cors(exchange -> researchObservation(exchange)));
        server.createContext("/v1/metrics/feedback", cors(exchange -> feedbackMetrics(exchange)));
        server.createContext("/v1/metrics/search", cors(exchange -> searchMetrics(exchange)));
        server.createContext("/metrics", cors(exchange -> prometheusMetrics(exchange)));
        server.createContext("/v1/research/export", cors(exchange -> researchExport(exchange)));
        server.createContext("/v1/research/manifest", cors(exchange -> researchManifest(exchange)));
    }

    public static LexicalSearchHttpServer create(int port, SearchCatalog catalog) throws IOException {
        return new LexicalSearchHttpServer(
                HttpServer.create(new InetSocketAddress(configuredBindAddress(), port), 0), catalog, null,
                ResearchStudyMetadata.defaults());
    }

    public static LexicalSearchHttpServer create(int port, SearchCatalog catalog, Path telemetryFile) throws IOException {
        return new LexicalSearchHttpServer(
                HttpServer.create(new InetSocketAddress(configuredBindAddress(), port), 0), catalog,
                PersistentTelemetryStore.open(telemetryFile), ResearchStudyMetadata.defaults());
    }

    public static LexicalSearchHttpServer create(int port, SearchCatalog catalog, Path telemetryFile,
                                                 ResearchStudyMetadata studyMetadata) throws IOException {
        return new LexicalSearchHttpServer(
                HttpServer.create(new InetSocketAddress(configuredBindAddress(), port), 0), catalog,
                PersistentTelemetryStore.open(telemetryFile), studyMetadata);
    }

    public static LexicalSearchHttpServer createFromSegmentDirectory(int port, Path directory) throws IOException {
        return create(port, SearchCatalog.fromSegmentDirectory(directory));
    }

    public static LexicalSearchHttpServer createFromMarkdownDirectory(int port, Path directory) throws IOException {
        SearchCatalog catalog = new SearchCatalog();
        catalog.indexMarkdownDirectory(directory);
        return create(port, catalog);
    }

    public static LexicalSearchHttpServer createFromMarkdownDirectory(int port, Path directory, Path telemetryFile) throws IOException {
        SearchCatalog catalog = new SearchCatalog();
        catalog.indexMarkdownDirectory(directory);
        return create(port, catalog, telemetryFile);
    }

    public static LexicalSearchHttpServer createFromMarkdownDirectory(int port, Path directory, Path telemetryFile,
                                                                       ResearchStudyMetadata studyMetadata) throws IOException {
        SearchCatalog catalog = new SearchCatalog();
        catalog.indexMarkdownDirectory(directory);
        return create(port, catalog, telemetryFile, studyMetadata);
    }

    public void start() {
        executor = Executors.newCachedThreadPool();
        server.setExecutor(executor);
        server.start();
    }

    public void stop() {
        server.stop(0);
        if (executor != null) executor.shutdownNow();
    }

    public int getPort() {
        return server.getAddress().getPort();
    }

    private void ready(HttpExchange exchange) throws IOException {
        respond(exchange, 200, "{\"status\":\"READY\",\"documents\":" + catalog.documentCount() + "}");
    }

    private final class IndexHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                respond(exchange, 405, "{\"error\":\"method not allowed\"}");
                return;
            }
            String sourcePath = exchange.getRequestHeaders().getFirst("X-Source-Path");
            if (sourcePath == null || sourcePath.trim().isEmpty()) sourcePath = "upload.md";
            String content = new String(readAll(exchange.getRequestBody()), StandardCharsets.UTF_8);
            try {
                com.nebula.ingestion.DocumentRecord document = catalog.indexMarkdown(sourcePath, content);
                registerTrustHeaders(exchange, sourcePath);
                respond(exchange, 201, "{\"documentId\":\"" + escape(document.getDocumentId())
                        + "\",\"title\":\"" + escape(document.getTitle())
                        + "\",\"documents\":" + catalog.documentCount() + "}");
            } catch (RuntimeException exception) {
                respond(exchange, 422, "{\"error\":\"" + escape(exception.getMessage()) + "\"}");
            }
        }
    }

    private final class SearchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!authorized(exchange)) return;
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
            int limit = 10;
            try {
                if (parameters.containsKey("limit")) limit = Integer.parseInt(parameters.get("limit"));
            if (limit < 1 || limit > 100) throw new NumberFormatException();
            } catch (NumberFormatException exception) {
                respond(exchange, 400, "{\"error\":\"limit must be between 1 and 100\"}");
                return;
            }
            if (applyResearchFault(exchange)) return;
            List<SearchResult> results;
            long started = System.nanoTime();
            if ("trust".equalsIgnoreCase(parameters.get("mode"))) {
                results = catalog.searchTrustAware(query, limit, System.currentTimeMillis());
            } else if ("semantic".equalsIgnoreCase(parameters.get("mode"))) {
                results = catalog.semanticSearch(query, limit);
            } else if ("hybrid".equalsIgnoreCase(parameters.get("mode"))) {
                results = catalog.hybridSearch(query, limit);
            } else if ("hnsw".equalsIgnoreCase(parameters.get("mode"))) {
                results = catalog.hnswSemanticSearch(query, limit);
            } else {
                results = catalog.search(query, limit);
            }
            if (telemetryStore != null && !consentedForResearch(exchange)) {
                // Search remains available without opting into persistent research telemetry.
            } else if (telemetryStore == null) {
                searchMetrics.record(normalizedMode(parameters.get("mode")), results.size(), System.nanoTime() - started);
            } else {
                telemetryStore.recordSearch(sessionId(exchange), taskId(exchange), query, normalizedMode(parameters.get("mode")), results.size(), System.nanoTime() - started);
            }
            respond(exchange, 200, searchJson(query, results));
        }
    }

    private void registerTrustHeaders(HttpExchange exchange, String sourcePath) {
        String authorityHeader = exchange.getRequestHeaders().getFirst("X-Source-Authority");
        String verifiedHeader = exchange.getRequestHeaders().getFirst("X-Last-Verified-Epoch-Millis");
        if (authorityHeader == null && verifiedHeader == null) return;
        try {
            double authority = authorityHeader == null ? 0.5 : Double.parseDouble(authorityHeader);
            long verified = verifiedHeader == null ? System.currentTimeMillis() : Long.parseLong(verifiedHeader);
            catalog.registerTrustMetadata(new DocumentTrustMetadata(
                    sourcePath, authority, verified, "api-client", "verified"));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("invalid trust metadata headers", exception);
        }
    }

    private final class SuggestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                respond(exchange, 405, "{\"error\":\"method not allowed\"}");
                return;
            }
            Map<String, String> parameters = queryParameters(exchange.getRequestURI().getRawQuery());
            String prefix = parameters.get("q");
            if (prefix == null) {
                respond(exchange, 400, "{\"error\":\"q is required\"}");
                return;
            }
            int limit = 10;
            try {
                if (parameters.containsKey("limit")) limit = Integer.parseInt(parameters.get("limit"));
                if (limit < 1 || limit > 100) throw new NumberFormatException();
            } catch (NumberFormatException exception) {
                respond(exchange, 400, "{\"error\":\"limit must be between 1 and 100\"}");
                return;
            }
            respond(exchange, 200, suggestionsJson(prefix, catalog.suggest(prefix, limit)));
        }
    }

    private final class DocumentHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                respond(exchange, 405, "{\"error\":\"method not allowed\"}");
                return;
            }
            String sourcePath = queryParameters(exchange.getRequestURI().getRawQuery()).get("path");
            if (sourcePath == null || sourcePath.trim().isEmpty()) {
                respond(exchange, 400, "{\"error\":\"path is required\"}");
                return;
            }
            com.nebula.ingestion.DocumentRecord document = catalog.documentBySourcePath(sourcePath);
            if (document == null) {
                respond(exchange, 404, "{\"error\":\"document not found\"}");
                return;
            }
            respond(exchange, 200, "{\"documentId\":\"" + escape(document.getDocumentId())
                    + "\",\"title\":\"" + escape(document.getTitle())
                    + "\",\"sourcePath\":\"" + escape(document.getSourcePath())
                    + "\",\"text\":\"" + escape(document.getText()) + "\"}");
        }
    }

    private final class FeedbackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                respond(exchange, 405, "{\"error\":\"method not allowed\"}");
                return;
            }
            String body = new String(readAll(exchange.getRequestBody()), StandardCharsets.UTF_8);
            try {
                FeedbackRecord feedback = new FeedbackRecord(
                        jsonString(body, "query"), jsonString(body, "mode"),
                        jsonString(body, "documentId"), jsonString(body, "sourcePath"),
                        jsonBoolean(body, "useful"));
                if (telemetryStore == null || consentedForResearch(exchange)) {
                    if (telemetryStore == null) feedbackStore.record(feedback);
                    else telemetryStore.recordFeedback(sessionId(exchange), taskId(exchange), feedback);
                }
                respond(exchange, 201, "{\"status\":\"recorded\",\"total\":" + feedbackStore.total() + "}");
            } catch (IllegalArgumentException exception) {
                respond(exchange, 400, "{\"error\":\"" + escape(exception.getMessage()) + "\"}");
            }
        }
    }

    private void feedbackMetrics(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            respond(exchange, 405, "{\"error\":\"method not allowed\"}");
            return;
        }
        respond(exchange, 200, "{\"total\":" + feedbackStore.total()
                + ",\"useful\":" + feedbackStore.usefulCount()
                + ",\"notUseful\":" + feedbackStore.notUsefulCount()
                + ",\"usefulRate\":" + feedbackStore.usefulRate() + "}");
    }

    private void searchMetrics(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            respond(exchange, 405, "{\"error\":\"method not allowed\"}");
            return;
        }
        StringBuilder body = new StringBuilder("{\"total\":").append(searchMetrics.getTotalSearches())
                .append(",\"zeroResults\":").append(searchMetrics.getZeroResultSearches())
                .append(",\"averageLatencyMs\":").append(searchMetrics.getAverageLatencyMillis())
                .append(",\"byMode\":{");
        int index = 0;
        for (Map.Entry<String, Integer> entry : searchMetrics.getSearchesByMode().entrySet()) {
            if (index++ > 0) body.append(',');
            body.append("\"").append(escape(entry.getKey())).append("\":").append(entry.getValue());
        }
        respond(exchange, 200, body.append("}}").toString());
    }

    private void prometheusMetrics(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            respond(exchange, 405, "# method not allowed\n", "text/plain; version=0.0.4; charset=utf-8");
            return;
        }
        StringBuilder body = new StringBuilder();
        body.append("# HELP nebula_search_requests_total Total search requests.\n")
                .append("# TYPE nebula_search_requests_total counter\n")
                .append("nebula_search_requests_total ").append(searchMetrics.getTotalSearches()).append('\n')
                .append("# HELP nebula_search_zero_results_total Searches returning no results.\n")
                .append("# TYPE nebula_search_zero_results_total counter\n")
                .append("nebula_search_zero_results_total ").append(searchMetrics.getZeroResultSearches()).append('\n')
                .append("# HELP nebula_search_latency_seconds_total Cumulative search latency.\n")
                .append("# TYPE nebula_search_latency_seconds_total counter\n")
                .append("nebula_search_latency_seconds_total ").append(searchMetrics.getTotalLatencySeconds()).append('\n')
                .append("# HELP nebula_search_latency_seconds_count Number of measured searches.\n")
                .append("# TYPE nebula_search_latency_seconds_count counter\n")
                .append("nebula_search_latency_seconds_count ").append(searchMetrics.getTotalSearches()).append('\n')
                .append("# HELP nebula_search_requests_by_mode_total Search requests grouped by ranking mode.\n")
                .append("# TYPE nebula_search_requests_by_mode_total counter\n");
        for (Map.Entry<String, Integer> entry : searchMetrics.getSearchesByMode().entrySet()) {
            body.append("nebula_search_requests_by_mode_total{mode=\"")
                    .append(prometheusLabel(entry.getKey())).append("\"} ")
                    .append(entry.getValue()).append('\n');
        }
        body.append("# HELP nebula_feedback_events_total Feedback events recorded.\n")
                .append("# TYPE nebula_feedback_events_total counter\n")
                .append("nebula_feedback_events_total ").append(feedbackStore.total()).append('\n');
        respond(exchange, 200, body.toString(), "text/plain; version=0.0.4; charset=utf-8");
    }

    private void researchExport(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            respond(exchange, 405, "{\"error\":\"method not allowed\"}");
            return;
        }
        if (telemetryStore == null) {
            respond(exchange, 503, "{\"error\":\"persistent telemetry is not enabled\"}");
            return;
        }
        String format = queryParameters(exchange.getRequestURI().getRawQuery()).getOrDefault("format", "json").toLowerCase();
        if (!"json".equals(format) && !"csv".equals(format)) {
            respond(exchange, 400, "{\"error\":\"format must be json or csv\"}");
            return;
        }
        String body = "csv".equals(format) ? telemetryStore.exportCsv() : telemetryStore.exportJson();
        String contentType = "csv".equals(format) ? "text/csv; charset=utf-8" : "application/json; charset=utf-8";
        exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=nebula-research." + format);
        respond(exchange, 200, body, contentType);
    }

    private void researchManifest(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            respond(exchange, 405, "{\"error\":\"method not allowed\"}");
            return;
        }
        respond(exchange, 200, "{\"studyVersion\":\"" + escape(studyMetadata.getStudyVersion())
                + "\",\"corpusVersion\":\"" + escape(studyMetadata.getCorpusVersion())
                + "\",\"studyWave\":\"" + escape(studyMetadata.getStudyWave())
                + "\",\"querySetVersion\":\"" + escape(studyMetadata.getQuerySetVersion())
                + "\",\"codeVersion\":\"" + escape(studyMetadata.getCodeVersion())
                + "\",\"eventTypes\":[\"search\",\"feedback\",\"task\",\"observation\"],"
                + "\"exportFormats\":[\"csv\",\"json\"],\"sessionId\":{\"source\":\"X-Session-Id\",\"anonymous\":true},"
                + "\"fields\":[\"type\",\"sessionId\",\"timestamp\",\"query\",\"mode\",\"results\","
                + "\"latencyNanos\",\"documentId\",\"sourcePath\",\"useful\",\"taskId\",\"action\",\"durationMs\",\"success\",\"confidence\",\"note\"],"
                + "\"privacy\":{\"identityCollection\":false,\"retention\":\"study-protocol-defined\"}}");
    }

    private void researchTask(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            respond(exchange, 405, "{\"error\":\"method not allowed\"}");
            return;
        }
        if (telemetryStore == null) {
            respond(exchange, 503, "{\"error\":\"persistent telemetry is not enabled\"}");
            return;
        }
        if (!consentedForResearch(exchange)) {
            respond(exchange, 403, "{\"error\":\"research consent is required\"}");
            return;
        }
        String body = new String(readAll(exchange.getRequestBody()), StandardCharsets.UTF_8);
        try {
            String taskId = jsonString(body, "taskId");
            String action = jsonString(body, "action").toLowerCase();
            if (!"start".equals(action) && !"complete".equals(action)) {
                throw new IllegalArgumentException("action must be start or complete");
            }
            boolean success = "complete".equals(action) && jsonBoolean(body, "success");
            long durationMs = body.contains("\"durationMs\"") ? jsonLong(body, "durationMs") : 0L;
            if (durationMs < 0L) throw new IllegalArgumentException("durationMs must not be negative");
            telemetryStore.recordTask(sessionId(exchange), taskId, action, success, durationMs);
            respond(exchange, 201, "{\"status\":\"recorded\",\"taskId\":\"" + escape(taskId) + "\",\"action\":\"" + action + "\"}");
        } catch (IllegalArgumentException exception) {
            respond(exchange, 400, "{\"error\":\"" + escape(exception.getMessage()) + "\"}");
        }
    }

    private void researchObservation(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            respond(exchange, 405, "{\"error\":\"method not allowed\"}");
            return;
        }
        if (telemetryStore == null) {
            respond(exchange, 503, "{\"error\":\"persistent telemetry is not enabled\"}");
            return;
        }
        if (!consentedForResearch(exchange)) {
            respond(exchange, 403, "{\"error\":\"research consent is required\"}");
            return;
        }
        String body = new String(readAll(exchange.getRequestBody()), StandardCharsets.UTF_8);
        try {
            String taskId = jsonString(body, "taskId");
            int confidence = (int) jsonLong(body, "confidence");
            String note = jsonString(body, "note");
            if (confidence < 1 || confidence > 5) throw new IllegalArgumentException("confidence must be between 1 and 5");
            if (note.length() > 2000) throw new IllegalArgumentException("note must be 2000 characters or fewer");
            telemetryStore.recordObservation(sessionId(exchange), taskId, confidence, note);
            respond(exchange, 201, "{\"status\":\"recorded\",\"taskId\":\"" + escape(taskId) + "\"}");
        } catch (IllegalArgumentException exception) {
            respond(exchange, 400, "{\"error\":\"" + escape(exception.getMessage()) + "\"}");
        }
    }

    private static String normalizedMode(String mode) {
        return mode == null || mode.trim().isEmpty() ? "bm25" : mode.toLowerCase();
    }

    private static String sessionId(HttpExchange exchange) {
        String value = exchange.getRequestHeaders().getFirst("X-Session-Id");
        if (value == null || value.trim().isEmpty()) return "anonymous";
        return value.length() > 128 ? value.substring(0, 128) : value;
    }

    private static String taskId(HttpExchange exchange) {
        String value = exchange.getRequestHeaders().getFirst("X-Task-Id");
        if (value == null || value.trim().isEmpty()) return "";
        return value.length() > 128 ? value.substring(0, 128) : value;
    }

    private static boolean consentedForResearch(HttpExchange exchange) {
        return "true".equalsIgnoreCase(exchange.getRequestHeaders().getFirst("X-Research-Consent"));
    }

    private static String searchJson(String query, List<SearchResult> results) {
        StringBuilder body = new StringBuilder("{\"query\":\"").append(escape(query)).append("\",\"results\":[");
        for (int i = 0; i < results.size(); i++) {
            SearchResult result = results.get(i);
            if (i > 0) body.append(',');
            body.append("{\"documentId\":\"").append(escape(result.getDocument().getDocumentId()))
                    .append("\",\"title\":\"").append(escape(result.getDocument().getTitle()))
                    .append("\",\"sourcePath\":\"").append(escape(result.getDocument().getSourcePath()))
                    .append("\",\"score\":").append(result.getScore())
                    .append(",\"termContributions\":{");
            int termIndex = 0;
            for (Map.Entry<String, Double> contribution : result.getTermContributions().entrySet()) {
                if (termIndex++ > 0) body.append(',');
                body.append("\"").append(escape(contribution.getKey())).append("\":")
                        .append(contribution.getValue());
            }
            body.append("}}");
        }
        return body.append("]}").toString();
    }

    private static String suggestionsJson(String prefix, List<String> suggestions) {
        StringBuilder body = new StringBuilder("{\"prefix\":\"").append(escape(prefix)).append("\",\"suggestions\":[");
        for (int i = 0; i < suggestions.size(); i++) {
            if (i > 0) body.append(',');
            body.append("\"").append(escape(suggestions.get(i))).append("\"");
        }
        return body.append("]}").toString();
    }

    private static Map<String, String> queryParameters(String rawQuery) throws IOException {
        Map<String, String> parameters = new HashMap<>();
        if (rawQuery == null || rawQuery.isEmpty()) return parameters;
        for (String pair : rawQuery.split("&")) {
            String[] keyValue = pair.split("=", 2);
            String key = URLDecoder.decode(keyValue[0], "UTF-8");
            String value = keyValue.length == 1 ? "" : URLDecoder.decode(keyValue[1], "UTF-8");
            parameters.put(key, value);
        }
        return parameters;
    }

    private static String jsonString(String body, String field) {
        Matcher matcher = Pattern.compile("\\\"" + Pattern.quote(field) + "\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"])*)\\\"").matcher(body);
        if (!matcher.find()) throw new IllegalArgumentException(field + " is required");
        return matcher.group(1).replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private static boolean jsonBoolean(String body, String field) {
        Matcher matcher = Pattern.compile("\\\"" + Pattern.quote(field) + "\\\"\\s*:\\s*(true|false)").matcher(body);
        if (!matcher.find()) throw new IllegalArgumentException(field + " is required");
        return Boolean.parseBoolean(matcher.group(1));
    }

    private static long jsonLong(String body, String field) {
        Matcher matcher = Pattern.compile("\\\"" + Pattern.quote(field) + "\\\"\\s*:\\s*(-?\\d+)").matcher(body);
        if (!matcher.find()) throw new IllegalArgumentException(field + " is required");
        return Long.parseLong(matcher.group(1));
    }

    private HttpHandler cors(HttpHandler handler) {
        return exchange -> {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type,X-Session-Id,X-Task-Id,X-Research-Consent,X-Source-Path,X-Source-Authority,X-Last-Verified-Epoch-Millis");
                respond(exchange, 204, "");
                return;
            }
            handler.handle(exchange);
        };
    }

    private void respond(HttpExchange exchange, int status, String body) throws IOException {
        respond(exchange, status, body, "application/json; charset=utf-8");
    }

    private void respond(HttpExchange exchange, int status, String body, String contentType) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", allowedOrigin.equals("*") ? "*" : allowedOrigin);
        exchange.getResponseHeaders().set("Vary", "Origin");
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.getResponseHeaders().set("Referrer-Policy", "no-referrer");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, status == 204 ? -1 : bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            if (status != 204) output.write(bytes);
        }
    }

    private static byte[] readAll(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int count;
        while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
        return output.toByteArray();
    }

    private static String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\r", "\\r").replace("\n", "\\n");
    }

    private static String prometheusLabel(String value) {
        return escape(value).replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r");
    }

    private static String configuredAllowedOrigin() {
        String value = System.getProperty("nebula.allowedOrigin");
        if (value == null || value.trim().isEmpty()) value = System.getenv("NEBULA_ALLOWED_ORIGIN");
        return value == null || value.trim().isEmpty() ? "*" : value.trim();
    }

    private boolean authorized(HttpExchange exchange) throws IOException {
        if (apiToken == null) return true;
        String header = exchange.getRequestHeaders().getFirst("Authorization");
        if (header == null || !header.equals("Bearer " + apiToken)) {
            exchange.getResponseHeaders().set("WWW-Authenticate", "Bearer");
            respond(exchange, 401, "{\"error\":\"authentication required\"}");
            return false;
        }
        return true;
    }

    private static String configuredApiToken() {
        String value = System.getProperty("nebula.apiToken");
        if (value == null || value.trim().isEmpty()) value = System.getenv("NEBULA_API_TOKEN");
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private static String configuredBindAddress() {
        String value = System.getenv("NEBULA_BIND_ADDRESS");
        return value == null || value.trim().isEmpty() ? "127.0.0.1" : value.trim();
    }

    /** Research-only deterministic fault hook; inactive unless explicitly configured. */
    private boolean applyResearchFault(HttpExchange exchange) throws IOException {
        String mode = System.getenv("NEBULA_FAULT_MODE");
        if (mode == null || mode.trim().isEmpty() || "none".equalsIgnoreCase(mode)) return false;
        long request = FAULT_REQUESTS.incrementAndGet();
        long every = positiveEnv("NEBULA_FAULT_EVERY", 1L);
        if (request % every != 0) return false;
        long delay = positiveEnv("NEBULA_FAULT_DELAY_MS", 0L);
        if ("latency".equalsIgnoreCase(mode) && delay > 0) {
            try { Thread.sleep(delay); } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                respond(exchange, 503, "{\"error\":\"injected interruption\"}");
                return true;
            }
            return false;
        }
        if ("http500".equalsIgnoreCase(mode) || "unavailable".equalsIgnoreCase(mode)) {
            respond(exchange, 500, "{\"error\":\"deterministic research fault\"}");
            return true;
        }
        throw new IllegalArgumentException("unsupported NEBULA_FAULT_MODE: " + mode);
    }

    private static long positiveEnv(String name, long fallback) {
        try {
            long value = Long.parseLong(System.getenv(name));
            return value > 0 ? value : fallback;
        } catch (Exception ignored) { return fallback; }
    }

    public static void main(String[] args) throws Exception {
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 8082;
        Path telemetryFile = args.length > 2 ? Paths.get(args[2]) : null;
        ResearchStudyMetadata metadata = args.length > 3
                ? new ResearchStudyMetadata(args[3], args.length > 4 ? args[4] : "corpus-v1",
                args.length > 5 ? args[5] : "wave-1", args.length > 6 ? args[6] : "query-set-v1",
                args.length > 7 ? args[7] : "unknown")
                : ResearchStudyMetadata.defaults();
        LexicalSearchHttpServer httpServer = args.length > 0 && telemetryFile != null
                ? createFromMarkdownDirectory(port, Paths.get(args[0]), telemetryFile, metadata)
                : args.length > 0
                ? createFromMarkdownDirectory(port, Paths.get(args[0]))
                : create(port, new SearchCatalog());
        httpServer.start();
        System.out.println("NEBULA lexical search listening on http://127.0.0.1:" + port);
    }
}
