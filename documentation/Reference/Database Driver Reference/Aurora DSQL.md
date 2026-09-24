---
subtitle: Aurora DSQL
---

- **Verified Versions:** N/A
- **Maintainer:** {% include community-db.html %}

## Supported Versions and Support Levels

{% include database-boilerplate.html %}

## Driver

| Item                                      | Details                                                                                                                                  |
|-------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------|
| **URL format**                            | <code>jdbc:aws-dsql:postgresql://<i>host</i>:<i>port</i>/<i>database</i></code>                                                          |
| **SSL support**                           | Required (Aurora DSQL enforces TLS; no flag needed)                                                                                      |
| **Ships with Flyway Command-line**        | No                                                                                                                                       |
| **Maven Central coordinates**             | `software.amazon.dsql:aurora-dsql-jdbc-connector`                                                                                        |
| **Supported versions**                    | `N/A`                                                                                                                                    |
| **Default Java class**                    | `software.amazon.dsql.jdbc.DSQLConnector`                                                                                                |
| **Flyway Community implementation**       | [flyway-community-db-support](https://github.com/flyway/flyway-community-db-support/tree/main/flyway-database-dsql)                      |

## CLI Usage

Aurora DSQL support is not bundled in the Flyway CLI. To use it:

- Download the [Aurora DSQL JDBC driver](https://central.sonatype.com/artifact/software.amazon.dsql/aurora-dsql-jdbc-connector) (or the [PostgreSQL JDBC driver](https://jdbc.postgresql.org/download/)) and drop it into the `drivers/` directory of your Flyway CLI installation.
- Download and drop the [flyway-database-dsql jar](https://central.sonatype.com/artifact/org.flywaydb/flyway-database-dsql) into `lib/flyway/`.

## Java Usage

### Maven

```xml

<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-dsql</artifactId>
</dependency>
```

You must also supply a JDBC driver on the classpath (either the Aurora DSQL connector or the PostgreSQL driver).

### Gradle

```groovy
buildscript {
    dependencies {
        implementation "org.flywaydb:flyway-database-dsql"
    }
}
```
