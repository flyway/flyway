---
subtitle: IRIS
---

- **Verified Versions:** N/A
- **Maintainer:** {% include community-db.html %}

## Supported Versions and Support Levels

{% include database-boilerplate.html %}

## Driver

| Item                                | Details                                                                                                 |
| ----------------------------------- |---------------------------------------------------------------------------------------------------------|
| **URL format**                      | <code>jdbc:IRIS://<i>host</i>:<i>port</i>/<i>namespace</i></code>                                       |
| **SSL support**                     | Not tested                                                                                              |
| **Ships with Flyway Command-line**  | No                                                                                                      |
| **Maven Central coordinates**       | `com.intersystems:intersystems-jdbc`                                                                   |
| **Supported versions**              | `N/A`                                                                                                   |
| **Default Java class**              | `com.intersystems.jdbc.IRISDriver`                                                                     |
| **Flyway Community implementation** | [flyway-community-db-support](https://github.com/flyway/flyway-community-db-support/tree/main/flyway-database-iris) |

## Java Usage

IRIS support is a separate dependency for Flyway and will need to be added to your Java project to access these features.

### Maven

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-iris</artifactId>
</dependency>
```

### Gradle

```groovy
buildscript {
    dependencies {
        implementation "org.flywaydb:flyway-database-iris"
    }
}
```

