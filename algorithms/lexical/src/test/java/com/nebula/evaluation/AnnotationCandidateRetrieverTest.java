package com.nebula.evaluation;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Exercises pooled retrieval provenance without requiring protected BGE data. */
public final class AnnotationCandidateRetrieverTest {
    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("nebula-pooled-retriever-");
        try {
            Path corpus = root.resolve("corpus");
            Path normalized = corpus.resolve("normalized");
            Path manifests = corpus.resolve("manifests");
            Files.createDirectories(normalized);
            Files.createDirectories(manifests);
            Files.write(normalized.resolve("d1.md"), "unrelated grounding placeholder".getBytes(StandardCharsets.UTF_8));
            Files.write(normalized.resolve("d2.md"), "alpha target configuration target alpha".getBytes(StandardCharsets.UTF_8));
            Files.write(normalized.resolve("d3.md"), "target alpha operational guidance".getBytes(StandardCharsets.UTF_8));
            Files.write(normalized.resolve("d4.md"), "unrelated material".getBytes(StandardCharsets.UTF_8));
            Path manifest = manifests.resolve("manifest.json");
            Files.write(manifest, ("{\n  \"documents\": [\n"
                    + "    {\n      \"document_id\": \"d1\",\n      \"text_path\": \"normalized/d1.md\",\n      \"title\": \"One\",\n      \"source_url\": \"https://example/1\"\n    },\n"
                    + "    {\n      \"document_id\": \"d2\",\n      \"text_path\": \"normalized/d2.md\",\n      \"title\": \"Two\",\n      \"source_url\": \"https://example/2\"\n    },\n"
                    + "    {\n      \"document_id\": \"d3\",\n      \"text_path\": \"normalized/d3.md\",\n      \"title\": \"Three\",\n      \"source_url\": \"https://example/3\"\n    },\n"
                    + "    {\n      \"document_id\": \"d4\",\n      \"text_path\": \"normalized/d4.md\",\n      \"title\": \"Four\",\n      \"source_url\": \"https://example/4\"\n    }\n"
                    + "  ]\n}\n").getBytes(StandardCharsets.UTF_8));
            Path queries = root.resolve("queries.psv");
            Files.write(queries, "query_id|query_text\nq1|alpha target\n".getBytes(StandardCharsets.UTF_8));
            Path split = root.resolve("split-manifest.json");
            Files.write(split, "{\"assignments\":{\"q1\":\"development\",\"q2\":\"test\"}}\n".getBytes(StandardCharsets.UTF_8));
            Path noTestSplit = root.resolve("no-test-split.json");
            Files.write(noTestSplit, "{\"assignments\":{\"q1\":\"development\"}}\n".getBytes(StandardCharsets.UTF_8));
            Path embeddingConfig = root.resolve("embedding-config.json");
            Files.write(embeddingConfig, ("{\"modelId\":\"BAAI/bge-base-en-v1.5\",\"revision\":\"a5beb1e3e68b9ab74eb54cfd186867f64f240e1a\"}\n").getBytes(StandardCharsets.UTF_8));
            Path outputA = root.resolve("out-a");
            Path outputB = root.resolve("out-b");
            Path emptyExclusions = root.resolve("excluded-empty.txt");
            Files.write(emptyExclusions, new byte[0]);
            Path spentExclusions = root.resolve("excluded-spent.txt");
            Files.write(spentExclusions, "q1\n".getBytes(StandardCharsets.UTF_8));
            String[] command = {queries.toString(), manifest.toString(), outputA.toString(), "2", "bm25,semantic,hybrid", "7", "1700000000000", "--split-manifest", split.toString()};
            AnnotationCandidateRetriever.main(command);
            command[2] = outputB.toString();
            AnnotationCandidateRetriever.main(command);
            try {
                AnnotationCandidateRetriever.main(new String[]{queries.toString(), manifest.toString(), root.resolve("missing-scope").toString(),
                        "2", "hybrid", "7", "1700000000000"});
                throw new AssertionError("missing split manifest must fail");
            } catch (IllegalArgumentException expected) {
                check(expected.getMessage().contains("--split-manifest"), "split manifest is mandatory");
            }

            Path candidatesA = outputA.resolve("retrieval-candidates.psv");
            String first = new String(Files.readAllBytes(candidatesA), StandardCharsets.UTF_8);
            String second = new String(Files.readAllBytes(outputB.resolve("retrieval-candidates.psv")), StandardCharsets.UTF_8);
            check(first.equals(second), "pooled output is deterministic");
            List<String> lines = Files.readAllLines(candidatesA, StandardCharsets.UTF_8);
            Set<String> documents = new HashSet<>();
            Set<String> systems = new HashSet<>();
            for (String line : lines.subList(1, lines.size())) {
                String[] fields = line.split("\\|", -1);
                check(fields.length == 11, "pooled schema includes provenance columns");
                check(documents.add(fields[2]), "pooled documents are deduplicated");
                check(!fields[9].isEmpty(), "system provenance is recorded");
                check(!fields[10].isEmpty(), "rank provenance is recorded");
                for (String system : fields[9].split(",")) systems.add(system);
                check(!"d1".equals(fields[2]), "grounding documents are not injected");
            }
            check(systems.size() > 1, "multiple retrieval systems contribute to the pool");
            check(documents.size() == lines.size() - 1, "one row per pooled document");
            String pooledManifest = new String(Files.readAllBytes(outputA.resolve("retrieval-manifest.json")), StandardCharsets.UTF_8);
            check(pooledManifest.contains("\"corpusDocumentCount\":4"), "all manifest documents were parsed");
            check(pooledManifest.contains("splitManifestSha256"), "split manifest checksum is recorded");
            System.out.println("AnnotationCandidateRetrieverTest: PASS");

            try {
                AnnotationCandidateRetriever.main(new String[]{queries.toString(), manifest.toString(), root.resolve("no-test").toString(),
                        "2", "hybrid", "7", "1700000000000", "--split-manifest", noTestSplit.toString()});
                throw new AssertionError("split manifest without test assignments must fail");
            } catch (IllegalArgumentException expected) {
                check(expected.getMessage().contains("no test assignments"), "missing test assignments are rejected");
            }

            try {
                AnnotationCandidateRetriever.main(new String[]{queries.toString(), manifest.toString(), root.resolve("excluded-run").toString(),
                        "2", "hybrid", "7", "1700000000000", "--split-manifest", split.toString(), "--exclude-query-ids", spentExclusions.toString()});
                throw new AssertionError("excluded query IDs must fail");
            } catch (IllegalArgumentException expected) {
                check(expected.getMessage().contains("excluded from pooling"), "spent query IDs are rejected");
            }
            Path unknownQueries = root.resolve("unknown-queries.psv");
            Files.write(unknownQueries, "query_id|query_text\nq99|alpha target\n".getBytes(StandardCharsets.UTF_8));
            try {
                AnnotationCandidateRetriever.main(new String[]{unknownQueries.toString(), manifest.toString(), root.resolve("unknown-run").toString(),
                        "2", "hybrid", "7", "1700000000000", "--split-manifest", split.toString()});
                throw new AssertionError("unknown query IDs must fail");
            } catch (IllegalArgumentException expected) {
                check(expected.getMessage().contains("absent from split manifest"), "unknown query IDs are rejected");
            }

            try {
                AnnotationCandidateRetriever.main(new String[]{queries.toString(), manifest.toString(), root.resolve("missing-cache").toString(),
                        "2", "bge-hybrid", "7", "1700000000000", "--split-manifest", split.toString(), "--exclude-query-ids", emptyExclusions.toString()});
                throw new AssertionError("BGE pooling without a cache must fail");
            } catch (IllegalArgumentException expected) {
                check(expected.getMessage().contains("embedding-cache"), "missing cache is rejected explicitly");
            }

            Path cache = root.resolve("bge-cache.tsv");
            StringBuilder cacheText = new StringBuilder()
                    .append("#modelId=BAAI/bge-base-en-v1.5\n")
                    .append("#revision=a5beb1e3e68b9ab74eb54cfd186867f64f240e1a\n")
                    .append("#license=Apache-2.0\n")
                    .append("#dimension=2\n")
                    .append("#normalized=true\n");
            for (String text : new String[]{"unrelated grounding placeholder", "alpha target configuration target alpha",
                    "target alpha operational guidance", "unrelated material", "alpha target"}) {
                cacheText.append(com.nebula.search.CachedEmbeddingModel.hash(text)).append("\t1,0\n");
            }
            Files.write(cache, cacheText.toString().getBytes(StandardCharsets.UTF_8));
            Path wrongCache = root.resolve("wrong-cache.tsv");
            Files.write(wrongCache, cacheText.toString().replace("a5beb1e3e68b9ab74eb54cfd186867f64f240e1a", "wrong-revision").getBytes(StandardCharsets.UTF_8));
            try {
                AnnotationCandidateRetriever.main(new String[]{queries.toString(), manifest.toString(), root.resolve("wrong-pin").toString(),
                        "2", "bge-hybrid", "7", "1700000000000", "--embedding-cache", wrongCache.toString(), "--embedding-config", embeddingConfig.toString(), "--split-manifest", split.toString(), "--exclude-query-ids", emptyExclusions.toString()});
                throw new AssertionError("wrong BGE revision must fail");
            } catch (IllegalArgumentException expected) {
                check(expected.getMessage().contains("configured BGE"), "wrong cache revision is rejected");
            }
            try {
                AnnotationCandidateRetriever.main(new String[]{queries.toString(), manifest.toString(), root.resolve("missing-time").toString(),
                        "2", "a3_hybrid_freshness", "7", "--embedding-cache", cache.toString(), "--embedding-config", embeddingConfig.toString(), "--split-manifest", split.toString(), "--exclude-query-ids", emptyExclusions.toString()});
                throw new AssertionError("freshness pooling without fixed time must fail");
            } catch (IllegalArgumentException expected) {
                check(expected.getMessage().contains("fixed nowEpochMillis"), "fixed time is required");
            }
            Path outputBge = root.resolve("out-bge");
            AnnotationCandidateRetriever.main(new String[]{queries.toString(), manifest.toString(), outputBge.toString(),
                    "2", "bge-hybrid,a3_hybrid_freshness", "7", "1700000000000", "--embedding-cache", cache.toString(), "--embedding-config", embeddingConfig.toString(), "--split-manifest", split.toString(), "--exclude-query-ids", emptyExclusions.toString()});
            String bgeManifest = new String(Files.readAllBytes(outputBge.resolve("retrieval-manifest.json")), StandardCharsets.UTF_8);
            check(bgeManifest.contains("BAAI/bge-base-en-v1.5@a5beb1e3e68b9ab74eb54cfd186867f64f240e1a"), "embedding model ID is recorded");
            check(bgeManifest.contains("bge-hybrid"), "BGE mode is recorded");
            check(bgeManifest.contains("embeddingCacheSha256"), "embedding cache checksum is recorded");
        } finally {
            deleteTree(root);
        }
    }

    private static void deleteTree(Path path) throws Exception {
        if (!Files.exists(path)) return;
        try (java.util.stream.Stream<Path> paths = Files.walk(path)) {
            paths.sorted(java.util.Comparator.reverseOrder()).forEach(item -> {
                try { Files.deleteIfExists(item); } catch (Exception error) { throw new RuntimeException(error); }
            });
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
