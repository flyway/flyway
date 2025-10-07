# Multi-stage build for aws-secretsmanager-jdbc shaded driver
# Stage 1: Clone source repository
ARG AWS_SECRETSMANAGER_JDBC_REPO=https://github.com/aws/aws-secretsmanager-jdbc.git
ARG AWS_SECRETSMANAGER_JDBC_VERSION=2.0.3
FROM alpine/git:latest AS clone
ARG AWS_SECRETSMANAGER_JDBC_REPO
ARG AWS_SECRETSMANAGER_JDBC_VERSION
WORKDIR /work
RUN git clone --depth 1 --branch "$AWS_SECRETSMANAGER_JDBC_VERSION" "$AWS_SECRETSMANAGER_JDBC_REPO" repo

# Stage 2: Merge partial POM using Python + lxml
FROM python:3.12-alpine AS merge
WORKDIR /merge
RUN apk add --no-cache bash
# Copy merge script and partial POM (build context root expected at flyway-shades/shades)
COPY merge_pom.py /merge/merge_pom.py
COPY aws-secretsmanager-jdbc/partial_pom.xml /merge/partial_pom.xml
COPY --from=clone /work/repo /merge/repo
RUN pip install --no-cache-dir lxml \
 && python /merge/merge_pom.py /merge/partial_pom.xml /merge/repo/pom.xml

# Stage 3: Build shaded jar with Maven
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build/repo
COPY --from=merge /merge/repo /build/repo
# Build (quiet except for errors). If failure, try to show any build logs.
RUN mvn -q -Pshade -DskipITs -DskipTests clean package || (echo "Maven build failed" >&2; ls -1 target || true; exit 1)

# Stage 4: Minimal image to export artifact
FROM alpine:3.19 AS artifact
ARG OUT_DIR=/out
ENV OUT_DIR=${OUT_DIR}
WORKDIR /artifact
# Copy any shaded jars
COPY --from=build /build/repo/target/aws-secretsmanager-jdbc-*-shaded.jar ./
COPY aws-secretsmanager-jdbc/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# Usage:
#  docker build -f flyway-shades/shades/aws-secretsmanager-jdbc/Dockerfile -t aws-sm-jdbc-shade:latest flyway-shades/shades
#  docker run --rm -v "$(pwd)/flyway-commandline/src/main/assembly/drivers/aws:/out" aws-sm-jdbc-shade:latest
# Result: shaded jar copied into drivers/aws preserving original filename.
ENTRYPOINT ["/entrypoint.sh"]
