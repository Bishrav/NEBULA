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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;

/** Local HTTP search service for the lexical retrieval milestone. */
public final class LexicalSearchHttpServer {
    private final HttpServer server;
    private final SearchCatalog catalog;

    private LexicalSearchHttpServer(HttpServer server, SearchCatalog catalog) {
        this.server = server;
        this.catalog = catalog;
        server.createContext("/health/live", exchange -> respond(exchange, 200, "{\"status\":\"UP\"}"));
        server.createContext("/health/ready", exchange -> ready(exchange));
        server.createContext("/v1/index/documents", new IndexHandler());
        server.createContext("/v1/search", new SearchHandler());
        server.createContext("/v1/suggest", new SuggestHandler());
    }

    public static LexicalSearchHttpServer create(int port, SearchCatalog catalog) throws IOException {
        return new LexicalSearchHttpServer(
                HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0), catalog);
    }

    public static LexicalSearchHttpServer createFromSegmentDirectory(int port, Path directory) throws IOException {
        return create(port, SearchCatalog.fromSegmentDirectory(directory));
    }

    public void start() {
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }

    public void stop() {
        server.stop(0);
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
            List<SearchResult> results;
            if ("trust".equalsIgnoreCase(parameters.get("mode"))) {
                results = catalog.searchTrustAware(query, limit, System.currentTimeMillis());
            } else if ("semantic".equalsIgnoreCase(parameters.get("mode"))) {
                results = catalog.semanticSearch(query, limit);
            } else if ("hnsw".equalsIgnoreCase(parameters.get("mode"))) {
                results = catalog.hnswSemanticSearch(query, limit);
            } else {
                results = catalog.search(query, limit);
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

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
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

    public static void main(String[] args) throws Exception {
        LexicalSearchHttpServer httpServer = create(8082, new SearchCatalog());
        httpServer.start();
        System.out.println("NEBULA lexical search listening on http://127.0.0.1:8082");
    }
}
