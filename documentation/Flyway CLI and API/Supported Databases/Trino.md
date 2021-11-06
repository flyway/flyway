---
subtitle: Trino
---
# Trino
- **Verified Versions:** N/A
- **Maintainer:** Community

## Supported Versions and Support Levels

{% include database-boilerplate.html %}

## Driver

| Item                               | Details                                                           |
|------------------------------------|-------------------------------------------------------------------|
| **URL format**                     | <code>jdbc:trino://<i>host</i>:<i>port</i>/<i>database</i></code> |
| **SSL support**                    | Not tested                                                        |
| **Ships with Flyway Command-line** | No                                                                |
| **Maven Central coordinates**      | `io.trino:trino-jdbc`                                             |
| **Supported versions**             | `N/A`                                                             |
| **Default Java class**             | `io.trino.jdbc.TrinoDriver`                                       |


## Java Usage

Trino support is a separate dependency for Flyway and must be added to your Java project to access these features.

### Maven

#### Open Source

```xml

<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-trino</artifactId>
</dependency>
```

### Gradle

#### Open Source

```groovy
dependencies {
    compile "org.flywaydb:flyway-database-trino"
}
```
