package com.nebula.search;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Verifies JSON and CSV research exports from the persistent HTTP server. */
public final class ResearchExportTest {
    public static void main(String[] args) throws Exception {
        Path file = Files.createTempDirectory("nebula-export-test").resolve("events.jsonl");
        SearchCatalog catalog = new SearchCatalog();
        catalog.indexMarkdown("docs/runbook.md", "# Shard failure\n\nRestart the shard safely.");
        LexicalSearchHttpServer server = LexicalSearchHttpServer.create(0, catalog, file);
        server.start();
        try {
            String base = "http://127.0.0.1:" + server.getPort();
            Response nonConsentedSearch = request("GET", base + "/v1/search?q=shard%20failure", "session-no-consent");
            check(nonConsentedSearch.status == 200, "search works without research consent");
            Response search = request("GET", base + "/v1/search?q=shard%20failure", "session-export", true);
            check(search.status == 200, "consented search is recorded");
            Response exportJson = request("GET", base + "/v1/research/export?format=json", null);
            check(exportJson.status == 200, "JSON export responds");
            check(exportJson.contentType.contains("application/json"), "JSON content type is returned");
            check(exportJson.body.contains("\"sessionId\":\"session-export\""), "JSON contains session ID");
            check(!exportJson.body.contains("session-no-consent"), "non-consented search is not persisted");
            Response exportCsv = request("GET", base + "/v1/research/export?format=csv", null);
            check(exportCsv.status == 200, "CSV export responds");
            check(exportCsv.contentType.contains("text/csv"), "CSV content type is returned");
            check(exportCsv.body.startsWith("type,sessionId,timestamp"), "CSV contains the research header");
            check(exportCsv.body.contains("session-export"), "CSV contains session data");
            Response manifest = request("GET", base + "/v1/research/manifest", null);
            check(manifest.status == 200, "research manifest responds");
            check(manifest.body.contains("\"studyVersion\":\"pilot-v1\""), "manifest contains study version");
            check(manifest.body.contains("\"identityCollection\":false"), "manifest states privacy boundary");
            System.out.println("ResearchExportTest: PASS");
        } finally {
            server.stop();
        }
    }

    private static Response request(String method, String url, String sessionId) throws Exception {
        return request(method, url, sessionId, false);
    }

    private static Response request(String method, String url, String sessionId, boolean consented) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod(method);
        if (sessionId != null) connection.setRequestProperty("X-Session-Id", sessionId);
        if (consented) connection.setRequestProperty("X-Research-Consent", "true");
        InputStream input = connection.getResponseCode() >= 400 ? connection.getErrorStream() : connection.getInputStream();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int count;
        while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
        return new Response(connection.getResponseCode(), connection.getHeaderField("Content-Type"),
                new String(output.toByteArray(), StandardCharsets.UTF_8));
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class Response {
        private final int status;
        private final String contentType;
        private final String body;

        private Response(int status, String contentType, String body) {
            this.status = status;
            this.contentType = contentType == null ? "" : contentType;
            this.body = body;
        }
    }
}
