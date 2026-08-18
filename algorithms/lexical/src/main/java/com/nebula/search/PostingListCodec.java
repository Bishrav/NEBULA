package com.nebula.search;

import java.util.List;

/** Compression facade for posting positions and future document ordinals. */
public final class PostingListCodec {
    private PostingListCodec() { }

    public static byte[] encodePositions(List<Integer> positions) {
        return DeltaCodec.encodeSorted(positions);
    }

    public static List<Integer> decodePositions(byte[] encoded) {
        return DeltaCodec.decodeSorted(encoded);
    }

    public static byte[] encodeDocumentOrdinals(List<Integer> sortedOrdinals) {
        return DeltaCodec.encodeSorted(sortedOrdinals);
    }

    public static List<Integer> decodeDocumentOrdinals(byte[] encoded) {
        return DeltaCodec.decodeSorted(encoded);
    }
}
