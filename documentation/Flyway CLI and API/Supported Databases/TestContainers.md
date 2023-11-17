---
subtitle: Testcontainers
---
# Testcontainers
- **Verified Versions:** N/A
- **Maintainer:** Redgate

## Supported Versions and Support Levels
For information regarding the supported version and support levels available,
please see [Supported Databases for Flyway](https://documentation.red-gate.com/flyway/learn-more-about-flyway/system-requirements/supported-databases-for-flyway)

For information regarding the Flyway features available, please see [Flyway feature glossary](https://documentation.red-gate.com/flyway/learn-more-about-flyway/feature-glossary)

## Driver

| Item                           | Details                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
|--------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| URL format                     | `jdbc:tc:` instead of `jdbc:` for your database                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| Ships with Flyway Command-line | No                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| Maven Central coordinates      | Database specific JARs:  <br>`org.testcontainers:cockroachdb:jar`  <br>`org.testcontainers:db2:jar`  <br>`org.testcontainers:mariadb:jar`  <br>`org.testcontainers:mssqlserver:jar`  <br>`org.testcontainers:mysql:jar`  <br>`org.testcontainers:oracle-xe:jar`  <br>`org.testcontainers:postgresql:jar`  <br>`org.testcontainers:tidb:jar`  <br>`org.testcontainers:yugabytedb:jar`  <br>Dependencies:  <br>`org.testcontainers:jdbc:jar`  <br>`org.testcontainers:database-commons:jar`  <br>`org.testcontainers:testcontainers:jar` |
| Supported versions             | `1.17.6`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| Default Java class             | `org.testcontainers.jdbc.ContainerDatabaseDriver`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |


- See the [Testcontainers documentation](https://www.testcontainers.org/modules/databases/jdbc/) for more information

### Compatibility

- See [Testcontainers list of supported databases](https://www.testcontainers.org/modules/databases/) to check if your chosen database is compatible

### Example URL

```
jdbc:tc:postgresql:11-alpine://localhost:5432/databasename
```

## Limitations

- If Flyway doesn't ship with a database driver for your chosen database, you will still need to provide one in order to use it with Testcontainers. For example, if you want to connect to a DB2 database with Testcontainers you will still need to provide a DB2 driver whose Maven Central coordinates are <code>com.ibm.db2.jcc:11.5.0.0</code>
