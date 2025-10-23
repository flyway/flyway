---
subtitle: Clickhouse
---

- **Verified Versions:** N/A
- **Maintainer:** {% include community-db.html %}

## Supported Versions and Support Levels

{% include database-boilerplate.html %}

## Driver

| Item                                      | Details                                                                                                                   |
|-------------------------------------------|---------------------------------------------------------------------------------------------------------------------------|
| **URL format**                            | <code>jdbc:clickhouse://<i>host</i>:<i>port</i>/<i>database</i></code>                                                    |
| **SSL support**                           | Not tested                                                                                                                |
| **Ships with Flyway Command-line**        | No                                                                                                                        |
| **JDBC driver download for Command-line** | [Clickhouse JDBC Drivers](https://clickhouse.com/docs/en/interfaces/jdbc)                                                 |
| **Maven Central coordinates**             | `com.clickhouse:clickhouse-jdbc`                                                                                          |
| **Supported versions**                    | `N/A`                                                                                                                     |
| **Default Java class**                    | `com.clickhouse.jdbc.ClickHouseDriver`                                                                                    |
| **Flyway Community implementation**       | [flyway-community-db-support](https://github.com/flyway/flyway-community-db-support/tree/main/flyway-database-clickhouse) |

## Related database-specific parameters
Whilst most databases Flyway supports have common configuration parameters, there are sometimes specific configurations available:
- [Clickhouse Cluster Name](<Configuration/Flyway Namespace/Flyway Clickhouse Namespace/Flyway Clickhouse Cluster Name Setting>)
- [Clickhouse Zookeeper Path](<Configuration/Flyway Namespace/Flyway Clickhouse Namespace/Flyway Clickhouse Zookeeper Path Setting>)

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
buildscript {
    dependencies {
        implementation "org.flywaydb:flyway-database-clickhouse"
    }
}
```

## Compression

By default, Clickhouse uses LZ4 compression. 
This requires [`lz4-java`](https://github.com/lz4/lz4-java) which needs to be [added to the classpath](<Usage/Adding to the classpath>). 
Alternatively, you can use a different algorithm by setting the `compress_algorithm` [JDBC property](<Configuration/Environments Namespace/Environment JDBC Properties Namespace>), or disable it by setting `compress` to false.
