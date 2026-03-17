FROM bash:5 AS untar
ARG FLYWAY_VERSION
ARG JACKSON_CORE_V2=2.21.1
ARG JACKSON_CORE_V3=3.1.0
WORKDIR /flyway
COPY flyway-commandline-${FLYWAY_VERSION}.tar.gz .
RUN apk add --no-cache wget \
  && gzip -d flyway-commandline-${FLYWAY_VERSION}.tar.gz \
  && tar -xf flyway-commandline-${FLYWAY_VERSION}.tar --strip-components=1 \
  && rm flyway-commandline-${FLYWAY_VERSION}.tar \
  && find /flyway -name 'jackson-core-*.jar' -delete \
  && wget -q -O /flyway/lib/jackson-core-${JACKSON_CORE_V2}.jar \
       "https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/${JACKSON_CORE_V2}/jackson-core-${JACKSON_CORE_V2}.jar" \
  && wget -q -O /flyway/lib/jackson-core-${JACKSON_CORE_V3}.jar \
       "https://repo1.maven.org/maven2/tools/jackson/core/jackson-core/${JACKSON_CORE_V3}/jackson-core-${JACKSON_CORE_V3}.jar" \
  && chmod -R a+r /flyway \
  && chmod a+x /flyway/flyway

FROM eclipse-temurin:21-jre-noble AS flyway
ARG FLYWAY_VERSION
LABEL "org.opencontainers.image.version"="${FLYWAY_VERSION}"
WORKDIR /flyway
COPY --from=untar /flyway /flyway
ENV PATH="/flyway:${PATH}"
ENTRYPOINT ["flyway"]
CMD ["-?"]
