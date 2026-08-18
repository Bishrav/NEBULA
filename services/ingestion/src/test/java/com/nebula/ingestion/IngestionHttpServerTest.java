package com.nebula.ingestion;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/** End-to-end smoke test for health, ingestion, and document retrieval. */
public final class IngestionHttpServerTest {
    public static void main(String[] args) throws Exception {
        IngestionService service = new IngestionService(
                new DocumentIngestor(), new InMemoryDocumentMetadataRepository());
        IngestionHttpServer server = IngestionHttpServer.create(0, service);
        server.start();
        try {
            String base = "http://127.0.0.1:" + server.getPort();
            Response health = request("GET", base + "/health/live", null, null);
            check(health.status == 200 && health.body.contains("UP"), "health endpoint responds");

            Response created = request("POST", base + "/v1/documents", "# API Guide\n\nUse the gateway.", "docs/api.md");
            check(created.status == 201, "document is created");
            check(created.body.contains("COMPLETED"), "job completes");
            String documentId = value(created.body, "documentId");

            Response document = request("GET", base + "/v1/documents/" + documentId, null, null);
            check(document.status == 200, "document can be retrieved");
            check(document.body.contains("API Guide"), "document title is returned");
            System.out.println("IngestionHttpServerTest: PASS");
        } finally {
            server.stop();
        }
    }

    private static Response request(String method, String url, String body, String sourcePath) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod(method);
        if (sourcePath != null) connection.setRequestProperty("X-Source-Path", sourcePath);
        if (body != null) {
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "text/markdown; charset=utf-8");
            connection.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
        }
        InputStream input = connection.getResponseCode() >= 400
                ? connection.getErrorStream() : connection.getInputStream();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int count;
        while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
        return new Response(connection.getResponseCode(), new String(output.toByteArray(), StandardCharsets.UTF_8));
    }

    private static String value(String json, String field) {
        String marker = "\"" + field + "\":\"";
        int start = json.indexOf(marker) + marker.length();
        int end = json.indexOf('"', start);
        return json.substring(start, end);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class Response {
        private final int status;
        private final String body;

        private Response(int status, String body) {
            this.status = status;
            this.body = body;
        }
    }
}
