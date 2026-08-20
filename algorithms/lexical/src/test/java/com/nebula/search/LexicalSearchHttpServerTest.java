package com.nebula.search;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** End-to-end test for document indexing and HTTP search. */
public final class LexicalSearchHttpServerTest {
    public static void main(String[] args) throws Exception {
        LexicalSearchHttpServer server = LexicalSearchHttpServer.create(0, new SearchCatalog());
        server.start();
        try {
            String base = "http://127.0.0.1:" + server.getPort();
            Response indexed = request("POST", base + "/v1/index/documents", "# Sharding\n\nConsistent hashing partitions documents.", "docs/sharding.md");
            check(indexed.status == 201, "document is indexed");

            Response search = request("GET", base + "/v1/search?q=" + URLEncoder.encode("consistent hashing", "UTF-8") + "&limit=5", null, null);
            check(search.status == 200, "search responds");
            check(search.body.contains("Sharding"), "search returns the indexed document");
            check(search.body.contains("termContributions"), "search returns ranking explanations");
            Response searchMetrics = request("GET", base + "/v1/metrics/search", null, null);
            check(searchMetrics.status == 200, "search metrics respond");
            check(searchMetrics.body.contains("\"total\":1"), "search metrics count requests");

            Response trustSearch = request("GET", base + "/v1/search?q=consistent%20hashing&mode=trust&limit=5", null, null);
            check(trustSearch.status == 200, "trust-aware search responds");
            check(trustSearch.body.contains("signal:authority"), "trust signals are returned");

            Response semanticSearch = request("GET", base + "/v1/search?q=consistent%20hashing&mode=semantic&limit=5", null, null);
            check(semanticSearch.status == 200, "semantic search responds");
            check(semanticSearch.body.contains("signal:semantic"), "semantic signal is returned");

            Response hybridSearch = request("GET", base + "/v1/search?q=consistent%20hashing&mode=hybrid&limit=5", null, null);
            check(hybridSearch.status == 200, "hybrid search responds");
            check(hybridSearch.body.contains("signal:hybrid"), "hybrid signal is returned");

            Response hnswSearch = request("GET", base + "/v1/search?q=consistent%20hashing&mode=hnsw&limit=5", null, null);
            check(hnswSearch.status == 200, "HNSW search responds");
            check(hnswSearch.body.contains("ann:efSearch"), "HNSW configuration is returned");

            Response document = request("GET", base + "/v1/documents?path=docs%2Fsharding.md", null, null);
            check(document.status == 200, "document preview responds");
            check(document.body.contains("Consistent hashing"), "document preview returns indexed evidence");

            Response feedback = request("POST", base + "/v1/feedback", "{\"query\":\"consistent hashing\",\"mode\":\"hybrid\",\"documentId\":\"demo\",\"sourcePath\":\"docs/sharding.md\",\"useful\":true}", null);
            check(feedback.status == 201, "feedback is recorded");
            Response feedbackMetrics = request("GET", base + "/v1/metrics/feedback", null, null);
            check(feedbackMetrics.status == 200, "feedback metrics respond");
            check(feedbackMetrics.body.contains("\"useful\":1"), "feedback metrics count useful events");

            Response suggestions = request("GET", base + "/v1/suggest?q=sha&limit=5", null, null);
            check(suggestions.status == 200, "suggestions respond");
            check(suggestions.body.contains("sharding"), "suggestions return indexed terms");

            Response missingQuery = request("GET", base + "/v1/search", null, null);
            check(missingQuery.status == 400, "missing query is rejected");
            System.out.println("LexicalSearchHttpServerTest: PASS");
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
