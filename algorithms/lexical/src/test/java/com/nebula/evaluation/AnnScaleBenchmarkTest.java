package com.nebula.evaluation;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class AnnScaleBenchmarkTest {
    public static void main(String[] args) throws Exception {
        Path output = Files.createTempFile("nebula-ann-scale", ".json");
        AnnScaleBenchmark.main(new String[]{output.toString(), "50", "8", "3", "5", "4", "10", "8", "2", "7"});
        String json = Files.readString(output, StandardCharsets.UTF_8);
        check(json.contains("\"vectors\":50"), "vector count is recorded");
        check(json.contains("\"M\":4"), "M is configurable");
        check(json.contains("\"efConstruction\":10"), "efConstruction is configurable");
        check(json.contains("\"efSearch\":8"), "efSearch is configurable");
        check(json.contains("\"repeat\":1"), "repeated trials are recorded");
        Files.deleteIfExists(output);
        System.out.println("AnnScaleBenchmarkTest: PASS");
    }

    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
