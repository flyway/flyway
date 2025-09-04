ARG BASE_IMAGE
FROM ${BASE_IMAGE} AS mongo
ARG MONGOSH_VERSION

COPY dockerfiles/mongo/install-mongosh.sh /tmp/install-mongosh.sh
RUN chmod +x /tmp/install-mongosh.sh && /tmp/install-mongosh.sh && rm /tmp/install-mongosh.sh
