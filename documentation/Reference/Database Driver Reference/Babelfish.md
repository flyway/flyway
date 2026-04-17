---
subtitle: Babelfish
---

- **Verified Versions:** Aurora PostgreSQL 16.4
- **Maintainer:** {% include redgate-badge.html %}

## Supported Versions and Support Levels

{% include database-boilerplate.html %}

## Overview

[Babelfish for Aurora PostgreSQL](https://aws.amazon.com/rds/aurora/babelfish/) is an AWS-managed capability that allows Aurora PostgreSQL to understand SQL Server wire protocol (TDS) and T-SQL. Flyway connects to Babelfish using the standard SQL Server JDBC driver on port 1433, so the same `flyway-sqlserver` dependency is used.

Flyway automatically detects a Babelfish endpoint by querying `SERVERPROPERTY('Babelfish')` and activates Babelfish-specific behaviour.

Babelfish reports itself as an older version of SQL Server so it is normal to see this in the console log:`"SQL Server 2014 is outside of Redgate community support"`

## Driver

| Item                               | Details                                                                                                                              |
|------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------|
| **URL format**                     | <code>jdbc:sqlserver://<i>cluster-endpoint</i>:1433;databaseName=<i>database</i></code>                                             |
| **SSL support**                    | Supported                                                                                                                            |
| **Ships with Flyway Command-line** | Yes                                                                                                                                  |
| **Maven Central coordinates**      | `com.microsoft.sqlserver:mssql-jdbc`                                                                                                 |
| **Supported versions**             | `10.0` and later                                                                                                                     |
| **Default Java class**             | `com.microsoft.sqlserver.jdbc.SQLServerDriver`                                                                                       |

## Java Usage

Babelfish support is included in the SQL Server dependency for Flyway.

### Maven

#### Redgate

```xml
<dependency>
    <groupId>com.redgate.flyway</groupId>
    <artifactId>flyway-sqlserver</artifactId>
</dependency>
```

#### Open Source

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-sqlserver</artifactId>
</dependency>
```

### Gradle

#### Redgate

```groovy
buildscript {
    dependencies {
        implementation "com.redgate.flyway:flyway-sqlserver"
    }
}
```

#### Open Source

```groovy
buildscript {
    dependencies {
        implementation "org.flywaydb:flyway-sqlserver"
    }
}
```

## Configuration

Use a non-default schema to avoid conflicts with the Babelfish-reserved `dbo` schema:

```toml
[environments.default]
url = "jdbc:sqlserver://my-cluster.cluster-abc123.eu-west-1.rds.amazonaws.com:1433;databaseName=master"
user = "admin"
password = "..."
defaultSchema = "my_schema"
```

## SQL Script Syntax

- Standard T-SQL syntax with statement delimiter **GO**
- `GO` is required to separate batch boundaries (e.g. between `DROP VIEW` and `CREATE VIEW`)

### Compatibility

Not all T-SQL syntax is supported by Babelfish. Refer to the [Babelfish compatibility documentation](https://babelfishpg.org/docs/usage/limitations-of-babelfish/) for the full list of unsupported features. Known incompatibilities encountered with Flyway include:

- `CREATE OR ALTER VIEW` is not supported. Use `DROP VIEW` + `GO` + `CREATE VIEW` instead:

```sql
IF OBJECT_ID('my_schema.my_view', 'V') IS NOT NULL DROP VIEW my_schema.my_view;
GO
CREATE VIEW my_schema.my_view AS
    SELECT id, name FROM my_schema.my_table;
```


### Example

```sql
/* Single line comment */
CREATE TABLE ${flyway:defaultSchema}.customers (
    customer_id INT NOT NULL,
    name        VARCHAR(100) NOT NULL
);
GO

/*
Multi-line
comment
*/
INSERT INTO ${flyway:defaultSchema}.customers (customer_id, name)
VALUES (1, 'Example Customer');
GO

-- Placeholder
INSERT INTO ${tableName} (name) VALUES ('Mr. T');
```

## Limitations

- Service Brokers are not supported.
- Assemblies (`CREATE ASSEMBLY`) are not supported.
- The `defaultSchema` property should be set to a non-`dbo` schema to avoid conflicts with Babelfish internals.


### Foundational capabilities only
Only foundation-level capabilities (migrations) are supported. Advanced capabilities like generation of SQL, state-based workflows or drift detection will not currently work