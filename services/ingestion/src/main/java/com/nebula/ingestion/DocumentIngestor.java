package com.nebula.ingestion;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Phase 1 ingestion core for Markdown documents.
 *
 * The output is deterministic: the same path and content produce the same
 * document identifier, normalized text, and content hash.
 */
public final class DocumentIngestor {
    private static final Pattern MARKDOWN_LINK = Pattern.compile("\\[([^]]+)]\\([^)]*\\)");
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
        String normalized = normalizeMarkdown(raw);
        String title = extractTitle(raw, path);
        String contentHash = sha256(normalized);
        String documentId = sha256(sourceType + "\n" + sourcePath + "\n" + contentHash);

        return new DocumentRecord(documentId, sourcePath, sourceType, title, normalized, contentHash);
    }

    private static String detectSourceType(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
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

    private static String extractTitle(String raw, Path path) {
        for (String line : raw.replace("\r\n", "\n").split("\n")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("# ")) {
                return trimmed.substring(2).trim();
            }
        }
        String name = path.getFileName().toString();
        int extension = name.lastIndexOf('.');
        return extension > 0 ? name.substring(0, extension) : name;
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
