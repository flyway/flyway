FROM bash:5 AS untar

WORKDIR /flyway

ARG FLYWAY_VERSION

COPY flyway-commandline-${FLYWAY_VERSION}-linux-alpine-x64.tar.gz .

RUN gzip -d flyway-commandline-${FLYWAY_VERSION}-linux-alpine-x64.tar.gz \
  && tar -xf flyway-commandline-${FLYWAY_VERSION}-linux-alpine-x64.tar --strip-components=1 \
  && rm flyway-commandline-${FLYWAY_VERSION}-linux-alpine-x64.tar \
  && rm -rf /flyway/jre \
  && chmod -R a+r /flyway \
  && chmod a+x /flyway/flyway

FROM eclipse-temurin:17-jre-alpine AS flyway

ARG FLYWAY_VERSION
LABEL "org.opencontainers.image.version"="${FLYWAY_VERSION}"

RUN apk --no-cache add --update bash openssl

WORKDIR /flyway

COPY --from=untar /flyway /flyway

ENV PATH="/flyway:${PATH}"

ENTRYPOINT ["flyway"]
CMD ["-?"]

FROM flyway AS redgate

ENV REDGATE_DOCKER=true
ARG SQLFLUFF_VERSION=3.0.7

RUN apk add --no-cache icu-libs krb5-libs libgcc libintl libssl3 libstdc++ zlib \
    && apk --no-cache add --update python3 py3-pip \
    && pip3 install --break-system-packages sqlfluff==${SQLFLUFF_VERSION}
