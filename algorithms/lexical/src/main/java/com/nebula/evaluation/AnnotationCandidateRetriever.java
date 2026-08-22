package com.nebula.evaluation;

import com.nebula.search.SearchCatalog;
import com.nebula.search.SearchResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Produces the private, fixed-retrieval candidate artifact used to populate
 * blinded annotation packets. It never injects query grounding documents.
 */
public final class AnnotationCandidateRetriever {
    private static final Pattern OBJECT = Pattern.compile("\\{\\s*\\n(.*?)\\n\\s*\\}", Pattern.DOTALL);
    private static final Pattern FIELD = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");
    private AnnotationCandidateRetriever() { }

    public static void main(String[] args) throws Exception {
        if (args.length < 3 || args.length > 7) {
            System.err.println("Usage: AnnotationCandidateRetriever <approved-queries.psv> <corpus-manifest.json> <output-dir> [topK] [hybrid|trust-oriented] [seed] [nowEpochMillis]");
            System.exit(2);
        }
        Path queriesPath = Paths.get(args[0]);
        Path manifestPath = Paths.get(args[1]);
        Path outputDir = Paths.get(args[2]);
        int topK = args.length > 3 ? Integer.parseInt(args[3]) : 10;
        String mode = args.length > 4 ? args[4] : "hybrid";
        long seed = args.length > 5 ? Long.parseLong(args[5]) : 20260822L;
        long now = args.length > 6 ? Long.parseLong(args[6]) : System.currentTimeMillis();
        if (topK <= 0 || !(mode.equals("hybrid") || mode.equals("trust-oriented"))) throw new IllegalArgumentException("invalid retrieval configuration");

        List<Query> queries = readQueries(queriesPath);
        List<ManifestDocument> manifest = readManifest(manifestPath);
        Map<String, ManifestDocument> byTextPath = new HashMap<>();
        SearchCatalog catalog = new SearchCatalog();
        Path root = manifestPath.getParent().getParent();
        for (ManifestDocument document : manifest) {
            Path textPath = root.resolve(document.textPath);
            catalog.indexMarkdown(document.textPath, new String(Files.readAllBytes(textPath), StandardCharsets.UTF_8));
            byTextPath.put(document.textPath.replace('\\', '/'), document);
        }
        outputDir.toFile().mkdirs();
        StringBuilder candidates = new StringBuilder("query_id|annotation_query_id|document_id|retrieval_rank|title|excerpt|score|source_path|source_url\n");
        StringBuilder key = new StringBuilder("annotation_query_id|original_query_id\n");
        int queryNumber = 0;
        for (Query query : queries) {
            String annotationId = String.format("AQ-%04d", ++queryNumber);
            key.append(annotationId).append('|').append(query.id).append('\n');
            List<SearchResult> results = mode.equals("hybrid")
                    ? catalog.hybridSearch(query.text, topK)
                    : catalog.searchTrustOrientedHybrid(query.text, topK, now, 0.70, 0.0, 0.14, 0.10, 0.06);
            int rank = 0;
            for (SearchResult result : results) {
                ManifestDocument document = byTextPath.get(result.getDocument().getSourcePath().replace('\\', '/'));
                if (document == null) throw new IllegalStateException("retrieved document missing from corpus manifest: " + result.getDocument().getSourcePath());
                candidates.append(query.id).append('|').append(annotationId).append('|').append(document.id).append('|')
                        .append(++rank).append('|').append(field(document.title)).append('|')
                        .append(field(excerpt(result.getDocument().getText(), 1200))).append('|')
                        .append(String.format(java.util.Locale.ROOT, "%.10f", result.getScore())).append('|')
                        .append(field(document.textPath)).append('|').append(field(document.sourceUrl)).append('\n');
            }
        }
        Files.write(outputDir.resolve("retrieval-candidates.psv"), candidates.toString().getBytes(StandardCharsets.UTF_8));
        Files.write(outputDir.resolve("PRIVATE-query-id-key.psv"), key.toString().getBytes(StandardCharsets.UTF_8));
        String manifestJson = "{\"schemaVersion\":\"annotation-retrieval-v1\",\"queryCount\":" + queries.size()
                + ",\"corpusDocumentCount\":" + manifest.size() + ",\"topK\":" + topK
                + ",\"mode\":\"" + mode + "\",\"lexicalWeight\":0.50,\"semanticWeight\":0.50"
                + ",\"trustLexicalWeight\":0.70,\"trustAuthorityWeight\":0.14,\"trustFreshnessWeight\":0.10,\"trustGraphWeight\":0.06"
                + ",\"freshnessHalfLifeDays\":30.0,\"seed\":" + seed + ",\"retrievalNowEpochMillis\":" + now
                + ",\"querySourceSha256\":\"" + sha256(queriesPath) + "\",\"corpusManifestSha256\":\"" + sha256(manifestPath) + "\""
                + ",\"groundingDocumentsInjected\":false,\"status\":\"PRIVATE_CANDIDATE_ARTIFACT\"}\n";
        Files.write(outputDir.resolve("retrieval-manifest.json"), manifestJson.getBytes(StandardCharsets.UTF_8));
        System.out.println("GENERATED: " + outputDir.toAbsolutePath());
    }

    private static List<Query> readQueries(Path path) throws IOException {
        List<Query> result = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        if (lines.isEmpty() || !lines.get(0).equals("query_id|query_text")) throw new IllegalArgumentException("approved query source must have query_id|query_text header");
        for (String line : lines.subList(1, lines.size())) {
            if (line.trim().isEmpty()) continue;
            String[] parts = line.split("\\|", 2);
            if (parts.length != 2 || parts[0].trim().isEmpty() || parts[1].trim().isEmpty()) throw new IllegalArgumentException("malformed approved query row");
            String id = parts[0].trim();
            if (!ids.add(id)) throw new IllegalArgumentException("duplicate approved query id: " + id);
            result.add(new Query(id, parts[1].trim()));
        }
        result.sort(Comparator.comparing(query -> query.id));
        if (result.isEmpty()) throw new IllegalArgumentException("approved query source is empty");
        return result;
    }

    private static List<ManifestDocument> readManifest(Path path) throws IOException {
        String text = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        List<ManifestDocument> result = new ArrayList<>();
        Matcher objects = OBJECT.matcher(text);
        while (objects.find()) {
            Map<String, String> fields = new HashMap<>();
            Matcher field = FIELD.matcher(objects.group(1));
            while (field.find()) fields.put(field.group(1), unescape(field.group(2)));
            if (fields.containsKey("document_id") && fields.containsKey("text_path")) {
                result.add(new ManifestDocument(fields.get("document_id"), fields.get("text_path"), fields.get("title"), fields.get("source_url")));
            }
        }
        if (result.isEmpty()) throw new IllegalArgumentException("corpus manifest contains no documents");
        return result;
    }

    private static String excerpt(String text, int limit) {
        String[] words = text.replaceAll("\\s+", " ").trim().split(" ");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < Math.min(limit, words.length); i++) { if (i > 0) result.append(' '); result.append(words[i]); }
        return result.toString();
    }

    private static String field(String value) { return value == null ? "" : value.replace("|", " ").replace("\r", " ").replace("\n", " "); }
    private static String unescape(String value) { return value.replace("\\\"", "\"").replace("\\\\", "\\"); }
    private static String sha256(Path path) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = Files.readAllBytes(path); byte[] hash = digest.digest(bytes);
        StringBuilder result = new StringBuilder(); for (byte value : hash) result.append(String.format("%02x", value & 0xff)); return result.toString();
    }

    private static final class Query { private final String id, text; private Query(String id, String text) { this.id = id; this.text = text; } }
    private static final class ManifestDocument {
        private final String id, textPath, title, sourceUrl;
        private ManifestDocument(String id, String textPath, String title, String sourceUrl) { this.id = id; this.textPath = textPath; this.title = title; this.sourceUrl = sourceUrl; }
    }
}
