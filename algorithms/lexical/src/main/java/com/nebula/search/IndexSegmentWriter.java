package com.nebula.search;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Writes a versioned immutable segment using an atomic temporary-file swap. */
public final class IndexSegmentWriter {
    private static final String MAGIC = "NEBULA_SEGMENT_V1";

    public void write(Path target, InvertedIndex index) throws IOException {
        if (target == null || index == null) throw new IllegalArgumentException("target and index are required");
        Path parent = target.toAbsolutePath().normalize().getParent();
        if (parent != null) Files.createDirectories(parent);
        Path temporary = target.resolveSibling(target.getFileName().toString() + ".tmp");
        try {
            try (OutputStream output = Files.newOutputStream(temporary);
                 DataOutputStream data = new DataOutputStream(output)) {
                data.writeUTF(MAGIC);
                List<IndexedDocument> documents = index.documents();
                data.writeInt(documents.size());
                for (IndexedDocument indexed : documents) writeDocument(data, indexed);

                List<String> terms = new ArrayList<>(index.terms());
                Collections.sort(terms);
                data.writeInt(terms.size());
                for (String term : terms) {
                    writeString(data, term);
                    List<Posting> postings = index.postings(term);
                    data.writeInt(postings.size());
                    for (Posting posting : postings) {
                        writeString(data, posting.getDocumentId());
                        byte[] encodedPositions = PostingListCodec.encodePositions(posting.getPositions());
                        data.writeInt(encodedPositions.length);
                        data.write(encodedPositions);
                    }
                }
            }
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static void writeDocument(DataOutputStream data, IndexedDocument indexed) throws IOException {
        com.nebula.ingestion.DocumentRecord document = indexed.getDocument();
        writeString(data, document.getDocumentId());
        writeString(data, document.getSourcePath());
        writeString(data, document.getSourceType());
        writeString(data, document.getTitle());
        writeString(data, document.getText());
        writeString(data, document.getContentHash());
        data.writeInt(indexed.getDocumentLength());
    }

    static void writeString(DataOutputStream data, String value) throws IOException {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        data.writeInt(bytes.length);
        data.write(bytes);
    }
}
