package com.nebula.search;

import java.util.Arrays;
import java.util.List;

/** Tests codec round trips and demonstrates compressed monotonic positions. */
public final class PostingListCodecTest {
    public static void main(String[] args) {
        List<Integer> positions = Arrays.asList(2, 8, 9, 40, 41);
        byte[] encoded = PostingListCodec.encodePositions(positions);
        List<Integer> decoded = PostingListCodec.decodePositions(encoded);
        check(decoded.equals(positions), "positions round-trip");
        check(encoded.length < positions.size() * 4, "encoded positions use fewer bytes than raw integers");

        List<Integer> ordinals = Arrays.asList(4, 7, 8, 1000);
        check(PostingListCodec.decodeDocumentOrdinals(
                PostingListCodec.encodeDocumentOrdinals(ordinals)).equals(ordinals), "document ordinals round-trip");
        System.out.println("PostingListCodecTest: PASS");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
