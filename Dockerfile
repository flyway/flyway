# initialize build and set base image for first stage
FROM maven:3.9-amazoncorretto-17 as stage1
# speed up Maven JVM a bit
ENV MAVEN_OPTS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1"
# set working directory
WORKDIR /opt/flyway

# copy your files
COPY flyway-commandline flyway-commandline
COPY flyway-community-db-support flyway-community-db-support
COPY flyway-core flyway-core
COPY flyway-database flyway-database
COPY flyway-gradle-plugin flyway-gradle-plugin
COPY flyway-maven-plugin flyway-maven-plugin
COPY flyway-sqlserver flyway-sqlserver
COPY LICENSE.txt .
COPY LICENSE.md .
COPY lombok.config .
COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .

# go-offline using the pom.xml
RUN mvn -B install -e --file pom.xml "-Dgithub.os=ubuntu-latest"

#Stage 2
# set base image for second stage
FROM maven:3.9-amazoncorretto-17
# set deployment directory
WORKDIR /flyway
# copy over the built artifact from the maven image
COPY --from=stage1 /root/.m2/repository/org/flywaydb/flyway-commandline/10.0.0/flyway-commandline-10.0.0-linux-x64.tar.gz /flyway

RUN gzip -d flyway-commandline-10.0.0-linux-x64.tar.gz \
  && tar -xf flyway-commandline-10.0.0-linux-x64.tar --strip-components=1 \
  && rm flyway-commandline-10.0.0-linux-x64* \
  && chmod -R a+r /flyway \
  && chmod a+x /flyway/flyway

ENV PATH="/flyway:${PATH}"

ENTRYPOINT ["flyway"]
CMD ["-?"]
