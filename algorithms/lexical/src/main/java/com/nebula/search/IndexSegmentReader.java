package com.nebula.search;

import com.nebula.ingestion.DocumentRecord;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Loads and validates an immutable NEBULA index segment. */
public final class IndexSegmentReader {
    private static final String MAGIC = "NEBULA_SEGMENT_V1";

    public PersistedIndexSegment read(Path source) throws IOException {
        try (InputStream input = Files.newInputStream(source);
             DataInputStream data = new DataInputStream(input)) {
            if (!MAGIC.equals(data.readUTF())) throw new IOException("unsupported NEBULA segment format");
            Map<String, IndexedDocument> documents = readDocuments(data);
            Map<String, List<Posting>> postings = readPostings(data);
            return new PersistedIndexSegment(documents, postings);
        }
    }

    private static Map<String, IndexedDocument> readDocuments(DataInputStream data) throws IOException {
        int count = checkedCount(data.readInt(), "document");
        Map<String, IndexedDocument> documents = new LinkedHashMap<>();
        for (int i = 0; i < count; i++) {
            String documentId = readString(data);
            String sourcePath = readString(data);
            String sourceType = readString(data);
            String title = readString(data);
            String text = readString(data);
            String contentHash = readString(data);
            int documentLength = data.readInt();
            DocumentRecord document = new DocumentRecord(documentId, sourcePath, sourceType, title, text, contentHash);
            documents.put(documentId, new IndexedDocument(document, documentLength));
        }
        return documents;
    }

    private static Map<String, List<Posting>> readPostings(DataInputStream data) throws IOException {
        int termCount = checkedCount(data.readInt(), "term");
        Map<String, List<Posting>> postingsByTerm = new LinkedHashMap<>();
        for (int termIndex = 0; termIndex < termCount; termIndex++) {
            String term = readString(data);
            int postingCount = checkedCount(data.readInt(), "posting");
            List<Posting> postings = new ArrayList<>();
            for (int postingIndex = 0; postingIndex < postingCount; postingIndex++) {
                String documentId = readString(data);
                int length = checkedCount(data.readInt(), "compressed posting");
                byte[] encodedPositions = new byte[length];
                data.readFully(encodedPositions);
                postings.add(new Posting(documentId, PostingListCodec.decodePositions(encodedPositions)));
            }
            postingsByTerm.put(term, postings);
        }
        return postingsByTerm;
    }

    static String readString(DataInputStream data) throws IOException {
        int length = checkedCount(data.readInt(), "string");
        if (length > 64 * 1024 * 1024) throw new IOException("segment string is too large");
        byte[] bytes = new byte[length];
        data.readFully(bytes);
        return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
    }

    private static int checkedCount(int value, String field) throws IOException {
        if (value < 0) throw new IOException("negative " + field + " count in segment");
        return value;
    }
}
