FROM eclipse-temurin:17-jdk AS build

WORKDIR /src
COPY services/ingestion/src/main/java services/ingestion/src/main/java
COPY algorithms/lexical/src/main/java algorithms/lexical/src/main/java

RUN mkdir -p build/classes \
    && find services/ingestion/src/main/java algorithms/lexical/src/main/java -name '*.java' -print > build/sources.txt \
    && javac -encoding UTF-8 -d build/classes @build/sources.txt

FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=build /src/build/classes /app/build/classes
COPY benchmarks/evaluation/corpus-v1 /app/corpus-v1

ENV NEBULA_STUDY_VERSION=pilot-v1 \
    NEBULA_CORPUS_VERSION=corpus-v1 \
    NEBULA_STUDY_WAVE=deployment-check \
    NEBULA_QUERY_SET_VERSION=query-set-v1 \
    NEBULA_CODE_VERSION=container \
    NEBULA_ALLOWED_ORIGIN=*

EXPOSE 8082
VOLUME ["/data"]

ENTRYPOINT ["java", "-cp", "/app/build/classes", "com.nebula.search.LexicalSearchHttpServer", "/app/corpus-v1", "8082", "/data/nebula-telemetry.jsonl"]
