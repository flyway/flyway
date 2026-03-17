FROM bash:5 AS untar
ARG FLYWAY_VERSION
ARG JACKSON_CORE_V2=2.21.1
ARG JACKSON_CORE_V2_SHA256=1edd5f2e49dca5f8e4519957c24b7b3050bd1c7ee883920da33cff031ff1f7c0
ARG JACKSON_CORE_V3=3.1.0
ARG JACKSON_CORE_V3_SHA256=4dd383f96b51b9b9ac4b74bdf1c150df0100443e1da1fba480f57da09a2dcef7
WORKDIR /flyway
COPY flyway-commandline-${FLYWAY_VERSION}-linux-alpine-x64.tar.gz .
RUN apk add --no-cache wget \
  && gzip -d flyway-commandline-${FLYWAY_VERSION}-linux-alpine-x64.tar.gz \
  && tar -xf flyway-commandline-${FLYWAY_VERSION}-linux-alpine-x64.tar --strip-components=1 \
  && rm flyway-commandline-${FLYWAY_VERSION}-linux-alpine-x64.tar \
  && find /flyway -name 'jackson-core-*.jar' -delete \
  && wget -q -O /flyway/lib/jackson-core-${JACKSON_CORE_V2}.jar \
       "https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/${JACKSON_CORE_V2}/jackson-core-${JACKSON_CORE_V2}.jar" \
  && echo "${JACKSON_CORE_V2_SHA256}  /flyway/lib/jackson-core-${JACKSON_CORE_V2}.jar" | sha256sum -c - \
  && wget -q -O /flyway/lib/jackson-core-${JACKSON_CORE_V3}.jar \
       "https://repo1.maven.org/maven2/tools/jackson/core/jackson-core/${JACKSON_CORE_V3}/jackson-core-${JACKSON_CORE_V3}.jar" \
  && echo "${JACKSON_CORE_V3_SHA256}  /flyway/lib/jackson-core-${JACKSON_CORE_V3}.jar" | sha256sum -c - \
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
