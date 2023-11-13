---
subtitle: YugabyteDB
---
# YugabyteDB
- **Verified Versions:** 2.4, 2.7
- **Maintainer:** Community

## Supported Versions and Support Levels
For information regarding the supported version and support levels available,
please see [Supported Databases for Flyway](https://documentation.red-gate.com/flyway/learn-more-about-flyway/system-requirements/supported-databases-for-flyway)

For information regarding the Flyway features available, please see [Flyway feature glossary](https://documentation.red-gate.com/flyway/learn-more-about-flyway/feature-glossary)



## Driver

| Item                               | Details                                                                |
|------------------------------------|------------------------------------------------------------------------|
| **URL format**                     | <code>jdbc:postgresql://<i>host</i>:<i>port</i>/<i>database</i></code> |
| **SSL support**                    | Yes - add `?ssl=true`                                                  |
| **Ships with Flyway Command-line** | Yes                                                                    |
| **Maven Central coordinates**      | `org.postgresql:postgresql`                                            |
| **Supported versions**             | `9.3-1104-jdbc4` and later                                             |
| **Default Java class**             | `org.postgresql.Driver`                                                |

## Java Usage

YugabyteDB support is a separate dependency for Flyway and will need to be added to your Java project to access these features.

### Maven
#### Open Source

```xml

<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-yugabytedb</artifactId>
</dependency>
```

### Gradle
#### Open Source

```groovy
dependencies {
    compile "org.flywaydb:flyway-database-yugabytedb"
}
```

## Notes

YugabyteDB is a variant of PostgreSQL and Flyway usage is the same for the two databases. For more details, 
please refer to the [PostgreSQL](Supported Databases/postgresql) page.

## Limitations

- AWS SecretsManager is not supported with YugabyteDB.
