package com.nebula.search;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/** Verifies configurable CORS preflight and browser-safety response headers. */
public final class CorsSecurityTest {
    public static void main(String[] args) throws Exception {
        System.setProperty("nebula.allowedOrigin", "http://localhost:5173");
        LexicalSearchHttpServer server = LexicalSearchHttpServer.create(0, new SearchCatalog());
        server.start();
        try {
            String base = "http://127.0.0.1:" + server.getPort();
            HttpURLConnection preflight = (HttpURLConnection) new URL(base + "/v1/research/manifest").openConnection();
            preflight.setRequestMethod("OPTIONS");
            preflight.setRequestProperty("Origin", "http://localhost:5173");
            check(preflight.getResponseCode() == 204, "CORS preflight responds");
            check("http://localhost:5173".equals(preflight.getHeaderField("Access-Control-Allow-Origin")), "configured origin is returned");
            check(preflight.getHeaderField("Access-Control-Allow-Headers").contains("X-Research-Consent"), "research header is allowed");

            HttpURLConnection manifest = (HttpURLConnection) new URL(base + "/v1/research/manifest").openConnection();
            manifest.setRequestProperty("Origin", "http://localhost:5173");
            drain(manifest.getInputStream());
            check(manifest.getResponseCode() == 200, "manifest responds");
            check("nosniff".equals(manifest.getHeaderField("X-Content-Type-Options")), "content type sniffing is disabled");
            check("no-referrer".equals(manifest.getHeaderField("Referrer-Policy")), "referrer policy is restricted");
            System.out.println("CorsSecurityTest: PASS");
        } finally {
            server.stop();
            System.clearProperty("nebula.allowedOrigin");
        }
    }

    private static void drain(InputStream input) throws Exception {
        try (InputStream stream = input; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[512];
            int count;
            while ((count = stream.read(buffer)) != -1) output.write(buffer, 0, count);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
