FROM bash:5 AS untar
ARG FLYWAY_VERSION
WORKDIR /flyway
COPY flyway-commandline-${FLYWAY_VERSION}-linux-alpine-x64.tar.gz .
RUN gzip -d flyway-commandline-${FLYWAY_VERSION}-linux-alpine-x64.tar.gz \
  && tar -xf flyway-commandline-${FLYWAY_VERSION}-linux-alpine-x64.tar --strip-components=1 \
  && rm flyway-commandline-${FLYWAY_VERSION}-linux-alpine-x64.tar \
  && chmod -R a+r /flyway \
  && chmod a+x /flyway/flyway

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
ENTRYPOINT ["flyway"]
CMD ["-?"]
