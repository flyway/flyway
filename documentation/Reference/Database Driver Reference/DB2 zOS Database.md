---
subtitle: DB2 z/OS
---

- **Verified Versions:** N/A
- **Maintainer:** {% include community-db.html %}

## Supported Versions and Support Levels

{% include database-boilerplate.html %}

## Driver

| Item                                | Details                                                                                                               |
| ----------------------------------- | --------------------------------------------------------------------------------------------------------------------- |
| **URL format**                      | <code>jdbc:db2://<i>host</i>:<i>port</i>/<i>database</i></code>                                                       |
| **SSL support**                     | Not tested                                                                                                            |
| **Ships with Flyway Command-line**  | No                                                                                                                    |
| **Maven Central coordinates**       | `com.ibm.db2.jcc`                                                                                                     |
| **Supported versions**              | `N/A`                                                                                                                 |
| **Default Java class**              | `com.ibm.db2.jcc.DB2Driver`                                                                                           |
| **Flyway Community implementation** | [flyway-community-db-support](https://github.com/flyway/flyway-community-db-support/tree/main/flyway-database-db2zos) |

## Related database-specific parameters
Whilst most databases Flyway supports have common configuration parameters, there are sometimes specific configurations available:
- [DB2 zOS Database Name](<Configuration/Flyway Namespace/Flyway DB2 zOS Namespace/Flyway DB2 zOS Database Name Setting>)
- [DB2 zOS SQL ID](<Configuration/Flyway Namespace/Flyway DB2 zOS Namespace/Flyway DB2 zOS SQL ID Setting>)

## Java Usage
DB2 support is a separate dependency for Flyway and will need to be added to your Java project to access these features.
DB2 is found within the `flyway-database-db2zos` plugin module.
### Maven
#### Open Source
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-db2zos</artifactId>
</dependency>
```

### Gradle
#### Open Source
```groovy
buildscript {
    dependencies {
        implementation "org.flywaydb:flyway-database-db2zos"
    }
}
```
