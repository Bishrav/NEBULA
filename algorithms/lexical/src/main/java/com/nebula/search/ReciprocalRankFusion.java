package com.nebula.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Rank-based fusion that avoids assuming compatible score distributions. */
public final class ReciprocalRankFusion {
    private final int rankConstant;

    public ReciprocalRankFusion(int rankConstant) {
        if (rankConstant <= 0) throw new IllegalArgumentException("rank constant must be positive");
        this.rankConstant = rankConstant;
    }

    public int getRankConstant() { return rankConstant; }

    public List<SearchResult> fuse(List<List<SearchResult>> rankedLists, int limit) {
        if (rankedLists == null || rankedLists.isEmpty()) throw new IllegalArgumentException("ranked lists are required");
        if (limit <= 0) throw new IllegalArgumentException("limit must be positive");
        Map<String, FusedCandidate> candidates = new LinkedHashMap<>();
        for (List<SearchResult> rankedList : rankedLists) {
            if (rankedList == null) continue;
            for (int index = 0; index < rankedList.size(); index++) {
                SearchResult result = rankedList.get(index);
                String id = result.getDocument().getDocumentId();
                FusedCandidate candidate = candidates.get(id);
                if (candidate == null) {
                    candidate = new FusedCandidate(result.getDocument());
                    candidates.put(id, candidate);
                }
                candidate.score += 1.0 / (rankConstant + index + 1.0);
            }
        }
        List<SearchResult> fused = new ArrayList<>();
        for (FusedCandidate candidate : candidates.values()) {
            Map<String, Double> explanation = new LinkedHashMap<>();
            explanation.put("signal:rrf", candidate.score);
            explanation.put("rrf:rank_constant", (double) rankConstant);
            fused.add(new SearchResult(candidate.document, candidate.score, explanation));
        }
        fused.sort(new Comparator<SearchResult>() {
            @Override
            public int compare(SearchResult left, SearchResult right) {
                int score = Double.compare(right.getScore(), left.getScore());
                return score != 0 ? score : left.getDocument().getDocumentId().compareTo(right.getDocument().getDocumentId());
            }
        });
        return Collections.unmodifiableList(new ArrayList<>(fused.subList(0, Math.min(limit, fused.size()))));
    }

    private static final class FusedCandidate {
        private final com.nebula.ingestion.DocumentRecord document;
        private double score;

        private FusedCandidate(com.nebula.ingestion.DocumentRecord document) {
            this.document = document;
        }
    }
}
