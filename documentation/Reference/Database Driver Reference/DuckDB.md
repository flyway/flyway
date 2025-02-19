---
subtitle: DuckDB
---
# DuckDB
- **Verified Versions:** N/A
- **Maintainer:** {% include community-db.html %}

## Supported Versions and Support Levels

{% include database-boilerplate.html %}

## Driver

| Item                                | Details                                                           |
| ----------------------------------- | ----------------------------------------------------------------- |
| **URL format**                      | <code>jdbc:duckdb:<i>database</i></code>                          |
| **SSL support**                     | Not tested                                                        |
| **Ships with Flyway Command-line**  | No                                                                |
| **JDBC driver download for Command-line** | [DuckDB JDBC Drivers](https://duckdb.org/docs/installation/?version=stable&environment=java&download_method=direct) |
| **Maven Central coordinates**       | `org.duckdb:duckdb_jdbc`                                          |
| **Supported versions**              | `N/A`                                                             |
| **Default Java class**              | `org.duckdb.DuckDBDriver`                                         |
| **Flyway Community implementation** | [flyway-community-db-support](https://github.com/flyway/flyway-community-db-support/tree/main/flyway-database-duckdb) |


## Java Usage

DuckDB support is a separate dependency for Flyway and will need to be added to your Java project to access these features.

### Maven

#### Open Source

```xml

<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-duckdb</artifactId>
</dependency>
```

### Gradle

#### Open Source

```groovy
dependencies {
    compile "org.flywaydb:flyway-database-duckdb"
}
```