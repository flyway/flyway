ARG BASE_IMAGE
FROM bash:5 AS untar
ARG SQLCL_VERSION="25.3.1.311.1257"
RUN apk add curl
RUN curl --connect-timeout 10 --max-time 120 --retry 2 --retry-delay 0 --retry-max-time 240 \
    "https://download.oracle.com/otn_software/java/sqldeveloper/sqlcl-$SQLCL_VERSION.zip" \
    -o /tmp/sqlcl.zip
RUN unzip /tmp/sqlcl.zip -d /tmp/sqlcl
FROM ${BASE_IMAGE} AS oracle

COPY --from=untar /tmp/sqlcl /opt/sqlcl
