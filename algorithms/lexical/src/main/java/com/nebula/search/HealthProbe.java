package com.nebula.search;

import java.net.HttpURLConnection;
import java.net.URL;

/** Minimal container health probe without requiring curl or wget in the image. */
public final class HealthProbe {
    private HealthProbe() { }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("health URL is required");
        HttpURLConnection connection = (HttpURLConnection) new URL(args[0]).openConnection();
        connection.setConnectTimeout(1000);
        connection.setReadTimeout(1000);
        if (connection.getResponseCode() != 200) System.exit(1);
    }
}
