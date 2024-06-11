---
subtitle: TimescaleDB
---
# TimescaleDB
- **Verified Versions:** 11, 12
- **Maintainer:** Community

## Supported Versions and Support Levels

{% include database-boilerplate.html %}

## Driver

| Item                               | Details                                                                |
|------------------------------------|------------------------------------------------------------------------|
| **URL format**                     | <code>jdbc:postgresql://<i>host</i>:<i>port</i>/<i>database</i></code> |
| **SSL support**                    | Yes - add `?ssl=true`                                                  |
| **Ships with Flyway Command-line** | Yes                                                                    |
| **Maven Central coordinates**      | `org.postgresql:postgresql`                                            |
| **Supported versions**             | `9.3-1104-jdbc4` and later                                             |
| **Default Java class**             | `org.postgresql.Driver`                                                |


## Notes

TimescaleDB is an extension to PostgreSQL and Flyway usage is the same for the two databases. For more details, 
please refer to the [PostgreSQL](<Supported Databases/postgresql-database>) page.

## Limitations

- AWS SecretsManager is not supported with TimescaleDB.
