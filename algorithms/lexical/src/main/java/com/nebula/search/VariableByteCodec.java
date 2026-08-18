package com.nebula.search;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Compact unsigned-integer codec using seven payload bits per byte. */
public final class VariableByteCodec {
    private VariableByteCodec() { }

    public static byte[] encode(List<Integer> values) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        for (Integer boxed : values) {
            if (boxed == null || boxed < 0) throw new IllegalArgumentException("values must be non-negative");
            int value = boxed;
            while (true) {
                int payload = value & 0x7f;
                value >>>= 7;
                if (value == 0) {
                    output.write(payload | 0x80);
                    break;
                }
                output.write(payload);
            }
        }
        return output.toByteArray();
    }

    public static List<Integer> decode(byte[] encoded) {
        if (encoded == null) throw new IllegalArgumentException("encoded bytes must not be null");
        if (encoded.length == 0) return Collections.emptyList();
        List<Integer> values = new ArrayList<>();
        int value = 0;
        int shift = 0;
        for (byte current : encoded) {
            int unsigned = current & 0xff;
            value |= (unsigned & 0x7f) << shift;
            if ((unsigned & 0x80) != 0) {
                values.add(value);
                value = 0;
                shift = 0;
            } else {
                shift += 7;
                if (shift > 28) throw new IllegalArgumentException("invalid variable-byte integer");
            }
        }
        if (shift != 0) throw new IllegalArgumentException("truncated variable-byte integer");
        return Collections.unmodifiableList(values);
    }
}
