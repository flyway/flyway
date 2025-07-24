---
subtitle: QuestDB
---

- **Verified Versions:** N/A
- **Maintainer:** {% include community-db.html %}

## Supported Versions and Support Levels

{% include database-boilerplate.html %}

## Driver

| Item                                | Details                                                                                                                |
| ----------------------------------- |------------------------------------------------------------------------------------------------------------------------|
| **URL format**                      | <code>jdbc:postgresql://<i>host</i>:<i>port</i>/<i>database</i></code>                                                 |
| **SSL support**                     | Not tested                                                                                                             |
| **Ships with Flyway Command-line**  | Yes                                                                                                                    |
| **Maven Central coordinates**       | `org.postgresql:postgresql`                                                                                             |
| **Supported versions**              | `N/A`                                                                                                                  |
| **Default Java class**              | `org.postgresql.Driver`                                                                                          |
| **Flyway Community implementation** | [flyway-community-db-support](https://github.com/flyway/flyway-community-db-support/tree/main/flyway-database-questdb) |

## Java Usage

QuestDB support is a separate dependency for Flyway and will need to be added to your Java project to access these features.

### Maven

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-questdb</artifactId>
</dependency>
```

### Gradle

```groovy
dependencies {
    implementation "org.flywaydb:flyway-database-questdb"
}
```

