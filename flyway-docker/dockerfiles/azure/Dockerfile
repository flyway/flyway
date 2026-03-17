FROM bash:5 AS untar
ARG FLYWAY_VERSION
ARG JACKSON_CORE_V2=2.21.1
ARG JACKSON_CORE_V3=3.1.0
WORKDIR /flyway
COPY flyway-commandline-${FLYWAY_VERSION}-linux-alpine-x64.tar.gz .
RUN apk add --no-cache wget \
  && gzip -d flyway-commandline-${FLYWAY_VERSION}-linux-alpine-x64.tar.gz \
  && tar -xf flyway-commandline-${FLYWAY_VERSION}-linux-alpine-x64.tar --strip-components=1 \
  && rm flyway-commandline-${FLYWAY_VERSION}-linux-alpine-x64.tar \
  && find /flyway -name 'jackson-core-*.jar' -delete \
  && wget -q -O /flyway/lib/jackson-core-${JACKSON_CORE_V2}.jar \
       "https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/${JACKSON_CORE_V2}/jackson-core-${JACKSON_CORE_V2}.jar" \
  && wget -q -O /flyway/lib/jackson-core-${JACKSON_CORE_V3}.jar \
       "https://repo1.maven.org/maven2/tools/jackson/core/jackson-core/${JACKSON_CORE_V3}/jackson-core-${JACKSON_CORE_V3}.jar" \
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
