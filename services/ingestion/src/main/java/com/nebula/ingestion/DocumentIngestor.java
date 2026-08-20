package com.nebula.ingestion;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Phase 1 ingestion core for Markdown documents.
 *
 * The output is deterministic: the same path and content produce the same
 * document identifier, normalized text, and content hash.
 */
public final class DocumentIngestor {
    private static final Pattern MARKDOWN_LINK = Pattern.compile("\\[([^]]+)]\\([^)]*\\)");
    private static final Pattern MARKDOWN_LINK_TARGET = Pattern.compile("\\[[^]]*]\\(([^)\\s]+)(?:\\s+\\\"[^\\\"]*\\\")?\\)");
    private static final Pattern HTML_TAG = Pattern.compile("<[^>]+>");
    private static final Pattern EMPHASIS = Pattern.compile("[*_~`]");
    private static final Pattern HEADING_PREFIX = Pattern.compile("^#{1,6}\\s*");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    public DocumentRecord ingest(Path path) throws IOException {
        if (path == null || !Files.isRegularFile(path)) {
            throw new IllegalArgumentException("path must point to a regular file");
        }

        String sourcePath = path.toAbsolutePath().normalize().toString();
        String sourceType = detectSourceType(path);
        String raw = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        return ingest(sourcePath, sourceType, raw, path.getFileName().toString());
    }

    /** Ingests content received from an API or connector without a temporary file. */
    public DocumentRecord ingest(String sourcePath, String rawContent) {
        if (sourcePath == null || sourcePath.trim().isEmpty()) {
            throw new IllegalArgumentException("sourcePath must not be blank");
        }
        String normalizedPath = sourcePath.replace('\\', '/');
        String sourceType = detectSourceType(normalizedPath);
        String fileName = normalizedPath.substring(normalizedPath.lastIndexOf('/') + 1);
        return ingest(normalizedPath, sourceType, rawContent, fileName);
    }

    private DocumentRecord ingest(String sourcePath, String sourceType, String raw,
                                  String fileName) {
        if (raw == null) throw new IllegalArgumentException("raw content must not be null");
        String normalized = normalizeMarkdown(raw);
        String title = extractTitle(raw, fileName);
        List<String> links = extractLinks(raw, sourcePath);
        String contentHash = sha256(normalized + "\nLINKS\n" + String.join("\n", links));
        String documentId = sha256(sourceType + "\n" + sourcePath + "\n" + contentHash);

        return new DocumentRecord(documentId, sourcePath, sourceType, title, normalized, contentHash, links);
    }

    private static String detectSourceType(Path path) {
        return detectSourceType(path.getFileName().toString());
    }

    private static String detectSourceType(String fileName) {
        String name = fileName.toLowerCase(Locale.ROOT);
        if (name.endsWith(".md") || name.endsWith(".markdown")) return "markdown";
        throw new IllegalArgumentException("unsupported document type: " + name);
    }

    static String normalizeMarkdown(String raw) {
        String text = raw.replace("\r\n", "\n").replace('\r', '\n');
        text = MARKDOWN_LINK.matcher(text).replaceAll("$1");
        text = HTML_TAG.matcher(text).replaceAll(" ");
        text = text.replaceAll("^\\s*[-*+]\\s+", "");
        text = text.replaceAll("^\\s*\\d+[.)]\\s+", "");
        text = HEADING_PREFIX.matcher(text).replaceAll("");
        text = EMPHASIS.matcher(text).replaceAll("");
        text = text.replace("|", " ");
        return WHITESPACE.matcher(text).replaceAll(" ").trim();
    }

    private static String extractTitle(String raw, String fileName) {
        for (String line : raw.replace("\r\n", "\n").split("\n")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("# ")) {
                return trimmed.substring(2).trim();
            }
        }
        String name = fileName;
        int extension = name.lastIndexOf('.');
        return extension > 0 ? name.substring(0, extension) : name;
    }

    static List<String> extractLinks(String raw, String sourcePath) {
        if (raw == null) return Collections.emptyList();
        Matcher matcher = MARKDOWN_LINK_TARGET.matcher(raw);
        List<String> links = new ArrayList<>();
        Path source = Paths.get(sourcePath);
        Path parent = source.getParent();
        while (matcher.find()) {
            String target = matcher.group(1).trim();
            if (target.startsWith("#") || target.startsWith("http://") || target.startsWith("https://")
                    || target.startsWith("mailto:")) continue;
            int fragment = target.indexOf('#');
            if (fragment >= 0) target = target.substring(0, fragment);
            if (target.isEmpty()) continue;
            Path resolved = parent == null ? Paths.get(target) : parent.resolve(target);
            String normalized = resolved.normalize().toString().replace('\\', '/');
            if (!links.contains(normalized)) links.add(normalized);
        }
        return Collections.unmodifiableList(links);
    }

    static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(bytes.length * 2);
            for (byte current : bytes) result.append(String.format("%02x", current & 0xff));
            return result.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
