ARG EDITION=flyway

FROM bash:5 AS untar

WORKDIR /flyway

ARG FLYWAY_VERSION

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

FROM flyway AS redgate

ENV REDGATE_DOCKER=true
ARG SQLFLUFF_VERSION

RUN apt-get update \
    && apt-get install -y --no-install-recommends python3-pip \
    && apt-get install -y --no-install-recommends libc6 libgcc1 libgcc-s1 libgssapi-krb5-2 libicu70 liblttng-ust1 libssl3 libstdc++6 libunwind8 zlib1g \
    && pip3 install sqlfluff==${SQLFLUFF_VERSION}

FROM $EDITION as mongo

RUN wget -qO- https://www.mongodb.org/static/pgp/server-8.0.asc | tee /etc/apt/trusted.gpg.d/server-8.0.asc
RUN echo "deb [ arch=amd64,arm64 ] https://repo.mongodb.org/apt/ubuntu jammy/mongodb-org/8.0 multiverse" | tee /etc/apt/sources.list.d/mongodb-org-8.0.list
RUN apt-get update
RUN apt-get install -y mongodb-mongosh=2.5.0