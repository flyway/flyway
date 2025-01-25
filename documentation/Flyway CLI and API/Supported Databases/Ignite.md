---
subtitle: Ignite
---
# Ignite (Thin)
- **Verified Versions:** N/A
- **Maintainer:** Community

## Supported Versions and Support Levels

{% include database-boilerplate.html %}

## Driver

| Item                               | Details                                                                 |
|------------------------------------|-------------------------------------------------------------------------|
| **URL format**                     | <code>jdbc:ignite:thin://<i>host</i>:<i>port</i>/<i>database</i></code> |
| **SSL support**                    | Not tested                                                              |
| **Ships with Flyway Command-line** | No                                                                      |
| **Maven Central coordinates**      | `org.apache.ignite:ignite-core`                                         |
| **Supported versions**             | `N/A`                                                                   |
| **Default Java class**             | `org.apache.ignite.IgniteJdbcThinDriver`                                |


## Java Usage

Ignite support is a separate dependency for Flyway and will need to be added to your Java project to access these features.

### Maven

#### Open Source

```xml

<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-ignite</artifactId>
</dependency>
```

### Gradle

#### Open Source

```groovy
dependencies {
    compile "org.flywaydb:flyway-database-ignite"
}
```