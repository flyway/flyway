FROM bash:5 AS untar
ARG FLYWAY_VERSION
WORKDIR /flyway
COPY flyway-commandline-${FLYWAY_VERSION}-linux-alpine-x64.tar.gz .
RUN gzip -d flyway-commandline-${FLYWAY_VERSION}-linux-alpine-x64.tar.gz \
  && tar -xf flyway-commandline-${FLYWAY_VERSION}-linux-alpine-x64.tar --strip-components=1 \
  && rm flyway-commandline-${FLYWAY_VERSION}-linux-alpine-x64.tar \
  && rm -rf /flyway/jre \
  && chmod -R a+r /flyway \
  && chmod a+x /flyway/flyway

FROM eclipse-temurin:25-jre-alpine AS flyway
ARG FLYWAY_VERSION
LABEL "org.opencontainers.image.version"="${FLYWAY_VERSION}"
RUN apk --no-cache add --update --upgrade bash openssl ca-certificates
RUN printf '%s\n' \
  40F6AF0346A99AA1CD1D555A4E9CCE62C7F9634603EE406615833DC8C8D00367 \
  86A1ECBA089C4A8D3BBE2734C612BA341D813E043CF9E8A862CD5C57A36BBE6B \
  125609AA301DA0A249B97A8239CB6A34216F44DCAC9F3954B14292F2E8C8608F \
  >> /opt/java/openjdk/lib/security/blocked.certs
WORKDIR /flyway
COPY --from=untar /flyway /flyway
ENV PATH="/flyway:${PATH}"
ENTRYPOINT ["flyway"]
CMD ["-?"]