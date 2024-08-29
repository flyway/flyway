---
subtitle: Clickhouse
---
# Clickhouse
- **Verified Versions:** N/A
- **Maintainer:** Community

## Supported Versions and Support Levels

{% include database-boilerplate.html %}

## Driver

| Item                                      | Details                                                                                     |
|-------------------------------------------|---------------------------------------------------------------------------------------------|
| **URL format**                            | <code>jdbc:clickhouse://<i>host</i>:<i>port</i>/<i>database</i></code>                      |
| **SSL support**                           | Not tested                                                                                  |
| **Ships with Flyway Command-line**        | No                                                                                          |
| **JDBC driver download for Command-line** | [Clickhouse JDBC Drivers](https://clickhouse.com/docs/en/interfaces/jdbc) |
| **Maven Central coordinates**             | `com.clickhouse:clickhouse-jdbc`                                                            |
| **Supported versions**                    | `N/A`                                                                                       |
| **Default Java class**                    | `com.clickhouse.jdbc.ClickHouseDriver`                                                      |


## Java Usage

Clickhouse support is a separate dependency for Flyway and will need to be added to your Java project to access these features.

### Maven

#### Open Source

```xml

<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-clickhouse</artifactId>
</dependency>
```

### Gradle

#### Open Source

```groovy
dependencies {
    compile "org.flywaydb:flyway-database-clickhouse"
}
```

## Compression

By default, Clickhouse uses LZ4 compression. 
This requires [`lz4-java`](https://github.com/lz4/lz4-java) which needs to be [added to the classpath](<Usage/Adding to the classpath>). 
Alternatively, you can use a different algorithm by setting the `compress_algorithm` [JDBC property](Configuration/Parameters/Environments/JDBC Properties), or disable it by setting `compress` to false.
