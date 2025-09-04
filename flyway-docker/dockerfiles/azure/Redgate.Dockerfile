ARG BASE_IMAGE=flyway:azure

FROM ${BASE_IMAGE} AS redgate
ARG SQLFLUFF_VERSION
ENV REDGATE_DOCKER=true

# Install Redgate dependencies and sqlfluff
RUN apk add --no-cache icu-libs krb5-libs libgcc libintl libssl3 libstdc++ zlib icu \
  && apk --no-cache add --update g++ python3 python3-dev py3-pip \
  && pip3 install --break-system-packages sqlfluff==${SQLFLUFF_VERSION}

