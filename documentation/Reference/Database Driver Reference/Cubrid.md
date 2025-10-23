---
subtitle: Cubrid
---

- **Verified Versions:** N/A
- **Maintainer:** {% include community-db.html %}

## Supported Versions and Support Levels

{% include database-boilerplate.html %}

## Driver

| Item                                | Details                                                                                                 |
| ----------------------------------- |---------------------------------------------------------------------------------------------------------|
| **URL format**                      | <code>jdbc:cubrid://<i>host</i>:<i>port</i></code>                                                      |
| **SSL support**                     | Not tested                                                                                              |
| **Ships with Flyway Command-line**  | No                                                                                                      |
| **Maven Central coordinates**       | `cubrid:cubrid-jdbc`                                                                                    |
| **Supported versions**              | `N/A`                                                                                                   |
| **Default Java class**              | `cubrid.jdbc.driver.CUBRIDDriver`                                                                       |
| **Flyway Community implementation** | [flyway-community-db-support](https://github.com/flyway/flyway-community-db-support/tree/main/flyway-database-cubrid) |

## Java Usage

Cubrid support is a separate dependency for Flyway and will need to be added to your Java project to access these features.


### Maven

Database support
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-cubrid</artifactId>
</dependency>
```

### Gradle

```groovy
buildscript {
    dependencies {
        implementation "org.flywaydb:flyway-database-cubrid"
    }
}
```