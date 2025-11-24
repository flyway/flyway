ARG BASE_IMAGE
FROM ${BASE_IMAGE} AS redgate
ARG SQLFLUFF_VERSION
ENV REDGATE_DOCKER=true

# Install Redgate dependencies and sqlfluff
RUN apt-get update && \
    apt-get install -y --no-install-recommends python3-pip && \
    apt-get install -y --no-install-recommends libc6 libgcc1 libgcc-s1 libgssapi-krb5-2 libicu74 liblttng-ust1 libssl3 libstdc++6 libunwind8 zlib1g && \
    pip3 install --break-system-packages sqlfluff==${SQLFLUFF_VERSION}

