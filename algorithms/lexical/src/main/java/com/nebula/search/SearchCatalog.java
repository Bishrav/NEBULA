package com.nebula.search;

import com.nebula.ingestion.DocumentIngestor;
import com.nebula.ingestion.DocumentRecord;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/** Application boundary joining document normalization with lexical search. */
public final class SearchCatalog {
    private final DocumentIngestor ingestor;
    private final InvertedIndex index;
    private final BM25SearchEngine searchEngine;
    private final TrustMetadataStore trustMetadata;
    private final TrustAwareSearchEngine trustSearchEngine;
    private final AutocompleteTrie autocomplete;
    private final LinkGraph linkGraph;
    private PageRankResult pageRank;

    public SearchCatalog() {
        this(new DocumentIngestor(), new InvertedIndex());
    }

    public SearchCatalog(DocumentIngestor ingestor, InvertedIndex index) {
        this.ingestor = ingestor;
        this.index = index;
        this.searchEngine = new BM25SearchEngine(index);
        this.trustMetadata = new TrustMetadataStore();
        this.trustSearchEngine = new TrustAwareSearchEngine(searchEngine, trustMetadata);
        this.autocomplete = new AutocompleteTrie();
        this.linkGraph = new LinkGraph();
        for (IndexedDocument document : index.documents()) linkGraph.add(document.getDocument());
        this.pageRank = PageRank.compute(linkGraph);
    }

    /** Creates a read-only search catalog restored from all .idx files in a directory. */
    public static SearchCatalog fromSegmentDirectory(Path directory) throws IOException {
        return new SearchCatalog(new DocumentIngestor(), new IndexSegmentLoader().loadDirectory(directory));
    }

    public DocumentRecord indexMarkdown(String sourcePath, String content) {
        DocumentRecord document = ingestor.ingest(sourcePath, content);
        if (index.add(document)) {
            for (String term : TextAnalyzer.analyze(document.getText())) autocomplete.addTerm(term, 1);
            linkGraph.add(document);
            pageRank = PageRank.compute(linkGraph);
        }
        return document;
    }

    public List<SearchResult> search(String query, int limit) {
        return searchEngine.search(query, limit);
    }

    public void registerTrustMetadata(DocumentTrustMetadata metadata) {
        trustMetadata.register(metadata);
    }

    public List<SearchResult> searchTrustAware(String query, int limit, long nowEpochMillis) {
        return new TrustAwareSearchEngine(searchEngine, trustMetadata, new FreshnessScorer(30.0),
                0.70, 0.20, 0.10, pageRank).search(query, limit, nowEpochMillis);
    }

    public List<SearchResult> searchTrustAware(String query, int limit, long nowEpochMillis,
                                               double lexicalWeight, double authorityWeight,
                                               double freshnessWeight) {
        return new TrustAwareSearchEngine(searchEngine, trustMetadata, new FreshnessScorer(30.0),
                lexicalWeight, authorityWeight, freshnessWeight, pageRank).search(query, limit, nowEpochMillis);
    }

    public int documentCount() {
        return index.documentCount();
    }

    public List<String> suggest(String prefix, int limit) {
        return autocomplete.suggest(prefix, limit);
    }

    public PageRankResult pageRank() { return pageRank; }
}
