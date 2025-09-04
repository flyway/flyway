FROM bash:5 AS untar
ARG FLYWAY_VERSION
WORKDIR /flyway
COPY flyway-commandline-${FLYWAY_VERSION}.tar.gz .
RUN gzip -d flyway-commandline-${FLYWAY_VERSION}.tar.gz \
  && tar -xf flyway-commandline-${FLYWAY_VERSION}.tar --strip-components=1 \
  && rm flyway-commandline-${FLYWAY_VERSION}.tar \
  && chmod -R a+r /flyway \
  && chmod a+x /flyway/flyway

FROM eclipse-temurin:21-jre-jammy AS flyway
ARG FLYWAY_VERSION
LABEL "org.opencontainers.image.version"="${FLYWAY_VERSION}"
WORKDIR /flyway
COPY --from=untar /flyway /flyway
ENV PATH="/flyway:${PATH}"
ENTRYPOINT ["flyway"]
CMD ["-?"]
