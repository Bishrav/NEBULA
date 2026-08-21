package com.nebula.search;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/** Tests bearer authentication and static endpoint discovery configuration. */
public final class ShardSecurityTest {
    public static void main(String[] args) throws Exception {
        System.setProperty("nebula.apiToken", "test-token");
        LexicalSearchHttpServer server = LexicalSearchHttpServer.create(0, new SearchCatalog());
        server.start();
        try {
            String base = "http://127.0.0.1:" + server.getPort();
            HttpURLConnection denied = request(base + "/v1/search?q=secure");
            check(denied.getResponseCode() == 401, "missing token is rejected");
            HttpShardClient client = new HttpShardClient("secure", base, 1000, 1, "test-token");
            check(client.search("secure", 5).isEmpty(), "authenticated client can query");
            ShardEndpointRegistry registry = ShardEndpointRegistry.parse("a=" + base + ",b=http://127.0.0.1:8089", 100, 2, "test-token");
            check(registry.clients().size() == 2, "registry parses endpoints");
            System.out.println("ShardSecurityTest: PASS");
        } finally { server.stop(); System.clearProperty("nebula.apiToken"); }
    }
    private static HttpURLConnection request(String url) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET"); return connection;
    }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
