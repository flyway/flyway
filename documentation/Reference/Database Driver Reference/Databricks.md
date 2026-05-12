---
subtitle: Databricks
---

- **Verified Versions:** N/A
- **Maintainer:** {% include redgate-badge.html %}

## Supported Versions and Support Levels

{% include database-boilerplate.html %}

## Driver

| Item                               | Details                                                                                                                                                                   |
| ---------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **URL format**                     | <code>jdbc:databricks://<i>host</i>:<i>port</i><i>/default;transportMode=http;ssl=1;httpPath=path;EnableArrow=0;AuthMech=3;UID=token;PWD=personal-access-token</i></code> |
| **SSL support**                    | Not tested                                                                                                                                                                |
| **Ships with Flyway Command-line** | Yes                                                                                                                                                                       |
| **Maven Central coordinates**      | `com.databricks:databricks-jdbc`                                                                                                                                          |
| **Supported versions**             | `N/A`                                                                                                                                                                     |
| **Default Java class**             | `com.databricks.client.jdbc.Driver`                                                                                                                                       |

## Java Usage

Databricks support is a separate dependency for Flyway and will need to be added to your Java project to access these features.


### Maven

#### Redgate

```xml
<dependency>
    <groupId>com.redgate.flyway</groupId>
    <artifactId>flyway-database-databricks</artifactId>
</dependency>
```

#### Open Source

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-databricks</artifactId>
</dependency>
```

### Gradle

#### Redgate

```groovy
buildscript {
    dependencies {
        implementation "com.redgate.flyway:flyway-database-databricks"
    }
}
```

#### Open Source

```groovy
buildscript {
    dependencies {
        implementation "org.flywaydb:flyway-database-databricks"
    }
}
```

## Limitations

- Due to a change in the Java 16+ runtime which Flyway depends upon, a formatting error will occur in the Databricks JDBC driver unless you define the handling of such, that can be done by either:
  - appending `EnableArrow=0` to your JDBC connection string
  - adding `--add-opens java.base/java.lang=ALL-UNNAMED`. This can be done via the [command line or environment variables](Usage/Command-Line)