package com.nebula.search;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** Loads one or more immutable segments into a searchable in-memory index. */
public final class IndexSegmentLoader {
    private final IndexSegmentReader reader;

    public IndexSegmentLoader() {
        this(new IndexSegmentReader());
    }

    public IndexSegmentLoader(IndexSegmentReader reader) {
        this.reader = reader;
    }

    public InvertedIndex load(List<Path> sources) throws IOException {
        if (sources == null) throw new IllegalArgumentException("sources must not be null");
        InvertedIndex index = new InvertedIndex();
        for (Path source : sources) index.addSegment(reader.read(source));
        return index;
    }

    public InvertedIndex load(Path... sources) throws IOException {
        List<Path> paths = new ArrayList<>();
        Collections.addAll(paths, sources);
        return load(paths);
    }

    public InvertedIndex loadDirectory(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) throw new IllegalArgumentException("directory must exist");
        List<Path> segments;
        try (java.util.stream.Stream<Path> stream = Files.list(directory)) {
            segments = stream.filter(path -> path.getFileName().toString().endsWith(".idx"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .collect(Collectors.toList());
        }
        return load(segments);
    }
}
