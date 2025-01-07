FROM bash:5 AS untar

WORKDIR /flyway

ARG FLYWAY_VERSION

COPY flyway-commandline-${FLYWAY_VERSION}-linux-alpine-x64.tar.gz .

RUN gzip -d flyway-commandline-${FLYWAY_VERSION}-linux-alpine-x64.tar.gz \
  && tar -xf flyway-commandline-${FLYWAY_VERSION}-linux-alpine-x64.tar --strip-components=1 \
  && rm flyway-commandline-${FLYWAY_VERSION}-linux-alpine-x64.tar \
  && chmod -R a+r /flyway \
  && chmod a+x /flyway/flyway

# Azure pipeline agents require specific things in their containers:
# https://docs.microsoft.com/en-us/azure/devops/pipelines/process/container-phases?view=azure-devops#linux-based-containers

FROM node:22-alpine AS flyway

ARG FLYWAY_VERSION
LABEL "org.opencontainers.image.version"="${FLYWAY_VERSION}"

ENV LANG C.UTF-8

RUN apk add --no-cache --virtual .pipeline-deps readline linux-pam \
  && apk add bash sudo shadow openssl curl tar \
  && apk del .pipeline-deps

LABEL "com.azure.dev.pipelines.agent.handler.node.path"="/usr/local/bin/node"

WORKDIR /flyway

COPY --from=untar /flyway /flyway
RUN ln -s /flyway/flyway /usr/local/bin/flyway

FROM flyway AS redgate

ENV REDGATE_DOCKER=true
ARG SQLFLUFF_VERSION=3.0.7

RUN apk add --no-cache icu-libs krb5-libs libgcc libintl libssl3 libstdc++ zlib icu \
  && apk --no-cache add --update g++ python3 python3-dev py3-pip \
  && pip3 install --break-system-packages sqlfluff==${SQLFLUFF_VERSION}
