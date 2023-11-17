---
subtitle: Ignite
---
# Ignite (Thin)
- **Verified Versions:** N/A
- **Maintainer:** Community

## Supported Versions and Support Levels
For information regarding the supported version and support levels available,
please see [Supported Databases for Flyway](https://documentation.red-gate.com/flyway/learn-more-about-flyway/system-requirements/supported-databases-for-flyway)

For information regarding the Flyway features available, please see [Flyway feature glossary](https://documentation.red-gate.com/flyway/learn-more-about-flyway/feature-glossary)

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