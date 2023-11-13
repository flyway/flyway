---
subtitle: SingleStoreDB
---

# SingleStoreDB
- **Verified Versions:** 7.1, 7.8
- **Maintainer:** Redgate

## Supported Versions and Support Levels
For information regarding the supported version and support levels available,
please see [Supported Databases for Flyway](https://documentation.red-gate.com/flyway/learn-more-about-flyway/system-requirements/supported-databases-for-flyway)

For information regarding the Flyway features available, please see [Flyway feature glossary](https://documentation.red-gate.com/flyway/learn-more-about-flyway/feature-glossary)

## Driver

| Item                               | Details                                                                 |
|------------------------------------|-------------------------------------------------------------------------|
| **URL format**                     | <code>jdbc:singlestore://<i>host</i>:<i>port</i>/<i>database</i></code> |
| **SSL support**                    | Yes - add `?useSsl=true`                                                |
| **Ships with Flyway Command-line** | Yes                                                                     |
| **Maven Central coordinates**      | `com.singlestore:singlestore-jdbc-client`                               |
| **Supported versions**             | `1.1.4` and later                                                       |
| **Default Java class**             | `com.singlestore.jdbc.Driver`                                           |


## Java Usage

SingleStoreDB support is a separate dependency for Flyway and will need to be added to your Java project to access these features.

### Maven
#### Redgate

```xml
<dependency>
    <groupId>com.redgate.flyway</groupId>
    <artifactId>flyway-singlestore</artifactId>
    <version>{{ site.flywayVersion }}</version>
</dependency>
```
#### Open Source
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-singlestore</artifactId>
    <version>{{ site.flywayVersion }}</version>
</dependency>
```
You will also need to [configure the repository](Usage/api-java)

### Gradle
#### Redgate
```groovy
dependencies {
    implementation "com.redgate.flyway:flyway-singlestore"
}
```
You will also need to [configure the repository](Usage/api-java)
