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
    private final EmbeddingModel embeddingModel;
    private final VectorIndex vectorIndex;
    private final SemanticSearchEngine semanticSearchEngine;
    private final HybridSearchEngine hybridSearchEngine;
    private final HnswIndex hnswIndex;
    private final HnswSemanticSearchEngine hnswSearchEngine;
    private PageRankResult pageRank;

    public SearchCatalog() {
        this(new DocumentIngestor(), new InvertedIndex(), new HashingEmbeddingModel(128));
    }

    public SearchCatalog(DocumentIngestor ingestor, InvertedIndex index) {
        this(ingestor, index, new HashingEmbeddingModel(128));
    }

    /** Creates a catalog with an explicit embedding model for controlled experiments. */
    public SearchCatalog(DocumentIngestor ingestor, InvertedIndex index, EmbeddingModel embeddingModel) {
        if (ingestor == null || index == null || embeddingModel == null) {
            throw new IllegalArgumentException("ingestor, index, and embedding model are required");
        }
        this.ingestor = ingestor;
        this.index = index;
        this.searchEngine = new BM25SearchEngine(index);
        this.trustMetadata = new TrustMetadataStore();
        this.trustSearchEngine = new TrustAwareSearchEngine(searchEngine, trustMetadata);
        this.autocomplete = new AutocompleteTrie();
        this.linkGraph = new LinkGraph();
        this.embeddingModel = embeddingModel;
        this.vectorIndex = new VectorIndex(embeddingModel.dimension());
        this.hnswIndex = new HnswIndex(embeddingModel.dimension(), 8, 64, 42L);
        for (IndexedDocument document : index.documents()) {
            double[] vector = embeddingModel.embed(document.getDocument().getText());
            vectorIndex.add(document.getDocument(), vector, embeddingModel.modelId());
            hnswIndex.add(document.getDocument(), vector, embeddingModel.modelId());
        }
        this.semanticSearchEngine = new SemanticSearchEngine(embeddingModel, vectorIndex);
        this.hybridSearchEngine = new HybridSearchEngine(searchEngine, semanticSearchEngine);
        this.hnswSearchEngine = new HnswSemanticSearchEngine(embeddingModel, hnswIndex, 32);
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
            double[] vector = embeddingModel.embed(document.getText());
            vectorIndex.add(document, vector, embeddingModel.modelId());
            hnswIndex.add(document, vector, embeddingModel.modelId());
            linkGraph.add(document);
            pageRank = PageRank.compute(linkGraph);
        }
        return document;
    }

    public List<SearchResult> search(String query, int limit) {
        return searchEngine.search(query, limit);
    }

    public List<SearchResult> semanticSearch(String query, int limit) {
        return semanticSearchEngine.search(query, limit);
    }

    public List<SearchResult> hybridSearch(String query, int limit) {
        return hybridSearchEngine.search(query, limit);
    }

    public List<SearchResult> hnswSemanticSearch(String query, int limit) {
        return hnswSearchEngine.search(query, limit);
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

    public DocumentRecord documentBySourcePath(String sourcePath) {
        if (sourcePath == null) return null;
        for (IndexedDocument document : index.documents()) {
            if (sourcePath.equals(document.getDocument().getSourcePath())) return document.getDocument();
        }
        return null;
    }

    public List<String> suggest(String prefix, int limit) {
        return autocomplete.suggest(prefix, limit);
    }

    public PageRankResult pageRank() { return pageRank; }
}
