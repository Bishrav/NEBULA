package com.nebula.ingestion;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

/** Minimal dependency-free HTTP API for local ingestion development. */
public final class IngestionHttpServer {
    private final HttpServer server;
    private final IngestionService service;
    private final Map<String, IngestionJob> jobs = new ConcurrentHashMap<>();

    private IngestionHttpServer(HttpServer server, IngestionService service) {
        this.server = server;
        this.service = service;
        registerRoutes();
    }

    public static IngestionHttpServer create(int port, IngestionService service) throws IOException {
        return new IngestionHttpServer(HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0), service);
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

    private void registerRoutes() {
        server.createContext("/health/live", exchange -> respond(exchange, 200, "{\"status\":\"UP\"}"));
        server.createContext("/health/ready", exchange -> respond(exchange, 200, "{\"status\":\"READY\"}"));
        server.createContext("/v1/documents", new DocumentsHandler());
        server.createContext("/v1/jobs", new JobsHandler());
    }

    private final class DocumentsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod()) && "/v1/documents".equals(path)) {
                ingest(exchange);
                return;
            }
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod()) && path.startsWith("/v1/documents/")) {
                getDocument(exchange, path.substring("/v1/documents/".length()));
                return;
            }
            respond(exchange, 405, "{\"error\":\"method not allowed\"}");
        }
    }

    private final class JobsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod()) && path.startsWith("/v1/jobs/")) {
                getJob(exchange, path.substring("/v1/jobs/".length()));
                return;
            }
            respond(exchange, 405, "{\"error\":\"method not allowed\"}");
        }
    }

    private void ingest(HttpExchange exchange) throws IOException {
        Headers headers = exchange.getRequestHeaders();
        String sourcePath = headers.getFirst("X-Source-Path");
        if (sourcePath == null || sourcePath.trim().isEmpty()) sourcePath = "upload.md";
        String content = new String(readAll(exchange.getRequestBody()), StandardCharsets.UTF_8);
        IngestionJob job = service.ingest(sourcePath, content);
        jobs.put(job.getJobId(), job);
        respond(exchange, job.getStatus() == IngestionStatus.COMPLETED ? 201 : 422, jobJson(job));
    }

    private void getJob(HttpExchange exchange, String jobId) throws IOException {
        IngestionJob job = jobs.get(jobId);
        if (job == null) {
            respond(exchange, 404, "{\"error\":\"job not found\"}");
            return;
        }
        respond(exchange, 200, jobJson(job));
    }

    private void getDocument(HttpExchange exchange, String documentId) throws IOException {
        Optional<VersionedDocument> result = service.findDocument(documentId);
        if (!result.isPresent()) {
            respond(exchange, 404, "{\"error\":\"document not found\"}");
            return;
        }
        VersionedDocument versioned = result.get();
        DocumentRecord document = versioned.getDocument();
        String body = "{"
                + "\"documentId\":\"" + escape(document.getDocumentId()) + "\","
                + "\"sourcePath\":\"" + escape(document.getSourcePath()) + "\","
                + "\"sourceType\":\"" + escape(document.getSourceType()) + "\","
                + "\"title\":\"" + escape(document.getTitle()) + "\","
                + "\"text\":\"" + escape(document.getText()) + "\","
                + "\"contentHash\":\"" + escape(document.getContentHash()) + "\","
                + "\"version\":" + versioned.getVersion()
                + "}";
        respond(exchange, 200, body);
    }

    private static String jobJson(IngestionJob job) {
        StringBuilder body = new StringBuilder("{");
        body.append("\"jobId\":\"").append(escape(job.getJobId())).append("\",");
        body.append("\"sourcePath\":\"").append(escape(job.getSourcePath())).append("\",");
        body.append("\"status\":\"").append(job.getStatus()).append("\",");
        body.append("\"version\":").append(job.getVersion());
        if (job.getDocumentId() != null) {
            body.append(",\"documentId\":\"").append(escape(job.getDocumentId())).append("\"");
        }
        if (job.getError() != null) {
            body.append(",\"error\":\"").append(escape(job.getError())).append("\"");
        }
        return body.append('}').toString();
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

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\r", "\\r").replace("\n", "\\n");
    }

    private static byte[] readAll(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int count;
        while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
        return output.toByteArray();
    }

    public static void main(String[] args) throws Exception {
        IngestionService service = new IngestionService(
                new DocumentIngestor(), new InMemoryDocumentMetadataRepository());
        IngestionHttpServer httpServer = create(8081, service);
        httpServer.start();
        System.out.println("NEBULA ingestion service listening on http://127.0.0.1:8081");
    }
}
