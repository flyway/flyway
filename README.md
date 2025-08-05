This fork:

- includes custom `flyway-database-vertica` plugin

To work with the repository you may need to use Java 17. On MacOS you can use sdkman to switch Java:

```
sdk install java 17.0.15-tem
sdk use java 17.0.15-tem
```

To build necessary packages (`flyway-core`, `flyway-database-vertica`, `flyway-mysql` and `flyway-parent`) and publish them to your local Maven repository:

```
./mvnw clean install -DskipTests -pl :flyway-mysql,:flyway-database-vertica,:flyway-core,:flyway-parent
```

If you want to install the package from your local Maven repository in an SBT project add the resolver to build.sbt:

```
resolvers += Resolver.mavenLocal
```

To publish the JAR to Zoined's S3 maven repository create Maven settings file in `~/.m2/settings.xml` with the following content:

```
<settings>
  <servers>
    <server>
      <id>aws-release</id>
      <username>your-access-key</username>
      <password>your-secret-key</password>
    </server>
  </servers>
</settings>
```

You probably already have necessary AWS access and secret keys in `~/.aws/credentials` configured for other projects.

This file is accessed by `maven-s3-wagon` extention that publishes the package to S3.

Then publish the packages (note that `11.0.4-zoined.0` is subject to change):

```
./mvnw deploy:deploy-file -Dfile=pom.xml -DgroupId=org.flywaydb -DartifactId=flyway-parent -Dversion=11.10.4-zoined.0 -Dpackaging=pom -DrepositoryId=aws-release -Durl=s3://maven.zoined.com/releases

./mvnw deploy:deploy-file -Dfile=flyway-core/target/flyway-core-11.10.4-zoined.0.jar -DgroupId=org.flywaydb -DartifactId=flyway-core -Dversion=11.10.4-zoined.0 -Dpackaging=jar -DrepositoryId=aws-release -Durl=s3://maven.zoined.com/releases

./mvnw deploy:deploy-file -Dfile=flyway-database/flyway-database-vertica/target/flyway-database-vertica-11.10.4-zoined.0.jar -DgroupId=org.flywaydb -DartifactId=flyway-database-vertica -Dversion=11.10.4-zoined.0 -Dpackaging=jar -DrepositoryId=aws-release -Durl=s3://maven.zoined.com/releases

./mvnw deploy:deploy-file -Dfile=flyway-database/flyway-mysql/target/flyway-mysql-11.10.4-zoined.0.jar -DgroupId=org.flywaydb -DartifactId=flyway-mysql -Dversion=11.10.4-zoined.0 -Dpackaging=jar -DrepositoryId=aws-release -Durl=s3://maven.zoined.com/releases
```

Note, we did not attempt to build any other packages or run the tests, further changes may be required to do so.




# [Flyway](https://github.com/flyway/flyway) by [Redgate](https://www.red-gate.com/) [![Build Release Tags](https://github.com/flyway/flyway/actions/workflows/build-release.yml/badge.svg)](https://github.com/flyway/flyway/actions/workflows/build-release.yml) [![Maven Central](https://img.shields.io/maven-central/v/org.flywaydb/flyway-core?logo=apachemaven&logoColor=red)](https://search.maven.org/artifact/org.flywaydb/flyway-core) [![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)

### Database Migrations Made Easy.

![Flyway](https://documentation.red-gate.com/download/attachments/138346876/FD?version=3&modificationDate=1633982869952&api=v2 "Flyway")


#### Evolve your database schema easily and reliably across all your instances.
Simple, focused and powerful.

#### Works on
Windows, macOS, Linux, Docker and Java

#### Supported build tools
Maven and Gradle

#### Supported databases
Aurora MySQL, Aurora PostgreSQL, Azure Synapse, Clickhouse, CockroachDB, Databricks, DB2, Derby, Firebird, Google BigQuery, Google Cloud Spanner, H2, HSQLDB, Informix, MariaDB, MongoDB, MySQL, Oracle, Percona XtraDB Cluster, PostgreSQL, Redshift, SAP HANA (Including SAP HANA Cloud), SingleStoreDB, Snowflake, SQLite, SQL Server, Sybase ASE, TiDB, TimescaleDB, YugabyteDB

#### Third party plugins
SBT, Ant, Spring Boot, Grails, Play!, DropWizard, Grunt, Griffon, Ninja, ...

## Documentation
- [Getting started guides](https://documentation.red-gate.com/flyway/getting-started-with-flyway)
- [Reference documentation](https://documentation.red-gate.com/flyway/reference)
- [Contributing to the project](https://flyway.github.io/flyway/)

## Download
You can download Flyway from [here](https://documentation.red-gate.com/flyway/reference/usage/flyway-open-source)

## About
Flyway is brought to you by [Redgate](https://www.red-gate.com/) with the help of many contributors.

- [Posts on changes and updates to Flyway](https://documentation.red-gate.com/fd/flyway-blog-205226034.html)
- [Release notes](https://documentation.red-gate.com/fd/release-notes-for-flyway-engine-179732572.html)

## How to contribute
Please visit our [contribution page](https://flyway.github.io/flyway/) to find out how you can contribute in various ways to the project.

## License
Copyright © [Red Gate Software Ltd](http://www.red-gate.com) 2010-2025

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

## Trademark
Flyway is a registered trademark of Boxfuse GmbH, owned by  [Red Gate Software Ltd](https://www.red-gate.com/).
