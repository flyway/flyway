FROM bash:5 AS untar
ARG FLYWAY_VERSION
WORKDIR /flyway
COPY flyway-commandline-${FLYWAY_VERSION}.tar.gz .
RUN gzip -d flyway-commandline-${FLYWAY_VERSION}.tar.gz \
  && tar -xf flyway-commandline-${FLYWAY_VERSION}.tar --strip-components=1 \
  && rm flyway-commandline-${FLYWAY_VERSION}.tar \
  && chmod -R a+r /flyway \
  && chmod a+x /flyway/flyway

FROM eclipse-temurin:25-jre-noble AS flyway
ARG FLYWAY_VERSION
LABEL "org.opencontainers.image.version"="${FLYWAY_VERSION}"
RUN printf '%s\n' \
  40F6AF0346A99AA1CD1D555A4E9CCE62C7F9634603EE406615833DC8C8D00367 \
  86A1ECBA089C4A8D3BBE2734C612BA341D813E043CF9E8A862CD5C57A36BBE6B \
  125609AA301DA0A249B97A8239CB6A34216F44DCAC9F3954B14292F2E8C8608F \
  >> /opt/java/openjdk/lib/security/blocked.certs
RUN apt-get update \
  && apt-get install -y --no-install-recommends --only-upgrade ca-certificates \
  && rm -rf /var/lib/apt/lists/*
WORKDIR /flyway
COPY --from=untar /flyway /flyway
ENV PATH="/flyway:${PATH}"
ENTRYPOINT ["flyway"]
CMD ["-?"]
