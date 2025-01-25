---
subtitle: CockroachDB
---
# CockroachDB
- **Verified Versions:** v21.1, v24.2
- **Maintainer:** Redgate

## Supported Versions and Support Levels

{% include database-boilerplate.html %}

## Driver

| Item                               | Details                                                                                                                               |
|------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------|
| **URL format**                     | <code>jdbc:postgresql://<i>host</i>:<i>port</i>/<i>database</i></code>                                                                |
| **SSL support**                    | [Yes](https://forum.cockroachlabs.com/t/connecting-to-an-ssl-secure-server-using-jdbc-java-and-client-certificate-authentication/400) |
| **Ships with Flyway Command-line** | Yes                                                                                                                                   |
| **Maven Central coordinates**      | `org.postgresql:postgresql`                                                                                                           |
| **Supported versions**             | `9.3-1104-jdbc4` and later                                                                                                            |
| **Default Java class**             | `org.postgresql.Driver`                                                                                                               |

## Java Usage
CockroachDB support is a separate dependency for Flyway and will need to be added to your Java project to access these features.
CockroachDB is found within the `flyway-database-postgresql` plugin module.
### Maven
#### Redgate
```xml
<dependency>
    <groupId>com.redgate.flyway</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```
#### Open Source
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

### Gradle
#### Redgate
```groovy
dependencies {
    implementation "com.redgate.flyway:flyway-database-postgresql"
}
```
#### Open Source
```groovy
dependencies {
    implementation "org.flywaydb:flyway-database-postgresql"
}
```

## SQL Script Syntax

- [Standard SQL syntax](Concepts/migrations#sql-based-migrations#syntax) with statement delimiter **;**

### Compatibility

- DDL exported by pg_dump can be used unchanged in a Flyway migration.
- Any CockroachDB sql script executed by Flyway, can be executed by the CockroachDB command-line tool and other
        PostgreSQL-compatible tools (after the placeholders have been replaced).

### Example

```sql
/* Single line comment */
CREATE TABLE test_data (
 value VARCHAR(25) NOT NULL PRIMARY KEY
);


/*
Multi-line
comment
*/

-- Placeholder
INSERT INTO ${tableName} (name) VALUES ('Mr. T');
```

## Limitations

- No support for psql meta-commands with no JDBC equivalent like `\set`

## Additional Information

- See CockroachDB's walkthrough on using Flyway [here](https://www.cockroachlabs.com/docs/stable/flyway.html)
