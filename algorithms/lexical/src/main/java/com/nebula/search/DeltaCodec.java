package com.nebula.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Delta encoding for sorted non-negative integer sequences. */
public final class DeltaCodec {
    private DeltaCodec() { }

    public static byte[] encodeSorted(List<Integer> sortedValues) {
        List<Integer> deltas = new ArrayList<>();
        int previous = 0;
        for (Integer value : sortedValues) {
            if (value == null || value < previous) throw new IllegalArgumentException("values must be sorted and non-negative");
            deltas.add(value - previous);
            previous = value;
        }
        return VariableByteCodec.encode(deltas);
    }

    public static List<Integer> decodeSorted(byte[] encoded) {
        List<Integer> deltas = VariableByteCodec.decode(encoded);
        if (deltas.isEmpty()) return Collections.emptyList();
        List<Integer> values = new ArrayList<>();
        int previous = 0;
        for (Integer delta : deltas) {
            if (Integer.MAX_VALUE - previous < delta) throw new IllegalArgumentException("delta sequence overflows integer range");
            previous += delta;
            values.add(previous);
        }
        return Collections.unmodifiableList(values);
    }
}
