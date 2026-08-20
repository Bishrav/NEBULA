package com.nebula.search;

/** Immutable provenance metadata exposed with the pilot research manifest. */
public final class ResearchStudyMetadata {
    private final String studyVersion;
    private final String corpusVersion;
    private final String studyWave;
    private final String querySetVersion;
    private final String codeVersion;

    public ResearchStudyMetadata(String studyVersion, String corpusVersion, String studyWave,
                                 String querySetVersion, String codeVersion) {
        this.studyVersion = required(studyVersion, "studyVersion");
        this.corpusVersion = required(corpusVersion, "corpusVersion");
        this.studyWave = required(studyWave, "studyWave");
        this.querySetVersion = required(querySetVersion, "querySetVersion");
        this.codeVersion = required(codeVersion, "codeVersion");
    }

    public static ResearchStudyMetadata defaults() {
        return new ResearchStudyMetadata(
                environmentOrDefault("NEBULA_STUDY_VERSION", "pilot-v1"),
                environmentOrDefault("NEBULA_CORPUS_VERSION", "corpus-v1"),
                environmentOrDefault("NEBULA_STUDY_WAVE", "wave-1"),
                environmentOrDefault("NEBULA_QUERY_SET_VERSION", "query-set-v1"),
                environmentOrDefault("NEBULA_CODE_VERSION", "unknown"));
    }

    public String getStudyVersion() { return studyVersion; }
    public String getCorpusVersion() { return corpusVersion; }
    public String getStudyWave() { return studyWave; }
    public String getQuerySetVersion() { return querySetVersion; }
    public String getCodeVersion() { return codeVersion; }

    private static String environmentOrDefault(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private static String required(String value, String field) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException(field + " is required");
        return value.trim();
    }
}
