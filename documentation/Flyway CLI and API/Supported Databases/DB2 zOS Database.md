---
subtitle: DB2 z/OS
---
# DB2 z/OS
- **Verified Versions:** N/A
- **Maintainer:** Community

## Supported Versions and Support Levels

{% include database-boilerplate.html %}

## Driver

| Item                               | Details                                                         |
|------------------------------------|-----------------------------------------------------------------|
| **URL format**                     | <code>jdbc:db2://<i>host</i>:<i>port</i>/<i>database</i></code> |
| **SSL support**                    | Not tested                                                      |
| **Ships with Flyway Command-line** | No                                                              |
| **Maven Central coordinates**      | `com.ibm.db2.jcc`                                               |
| **Supported versions**             | `N/A`                                                           |
| **Default Java class**             | `com.ibm.db2.jcc.DB2Driver`                                     |


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
dependencies {
    implementation "org.flywaydb:flyway-database-db2zos"
}
```
