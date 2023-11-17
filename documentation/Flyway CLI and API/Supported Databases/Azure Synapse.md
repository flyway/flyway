---
subtitle: Azure Synapse
---
# Azure Synapse
- **Verified Versions:** Latest
- **Maintainer:** Redgate

## Supported Versions and Support Levels
For information regarding the supported version and support levels available,
please see [Supported Databases for Flyway](https://documentation.red-gate.com/flyway/learn-more-about-flyway/system-requirements/supported-databases-for-flyway)

For information regarding the Flyway features available, please see [Flyway feature glossary](https://documentation.red-gate.com/flyway/learn-more-about-flyway/feature-glossary)

## Driver

| Item                               | Details                                                                                                                              |
|------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------|
| **URL format**                     | <code>jdbc:sqlserver://<i>host</i>:<i>port</i>;databaseName=<i>database</i></code>                                                   |
| **SSL support**                    | [Yes](https://docs.microsoft.com/en-us/sql/connect/jdbc/connecting-with-ssl-encryption?view=sql-server-ver15) \- add `;encrypt=true` |
| **Ships with Flyway Command-line** | Yes                                                                                                                                  |
| **Maven Central coordinates**      | `com.microsoft.sqlserver:mssql-jdbc`                                                                                                 |
| **Supported versions**             | `4.0` and later                                                                                                                      |
| **Default Java class**             | `com.microsoft.sqlserver.jdbc.SQLServerDriver`                                                                                       |


## Java Usage
Azure Synapse support is a separate dependency for Flyway and will need to be added to your Java project to access these features.
Azure Synapse is found within the `flyway-sqlserver` plugin module.

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
dependencies {
    compile "com.redgate.flyway:flyway-sqlserver"
}
```

#### Open Source
```groovy
dependencies {
    compile "org.flywaydb:flyway-sqlserver"
}
```

## Azure Synapse Syntax

- [See SQL Server](Supported Databases/SQL Server#sql-server-syntax)

### Compatibility

- [See SQL Server](Supported Databases/SQL Server#compatibility)

### Example

```sql
/* Single line comment */
CREATE TABLE test_user (
  id INT NOT NULL,
  name VARCHAR(25) NOT NULL,  -- this is a valid ' comment
  PRIMARY KEY NONCLUSTERED (name) NOT ENFORCED
);
GO

-- Placeholder
INSERT INTO ${tableName} (name) VALUES ('Mr. T');
```

## Authentication

[See SQL Server](Supported Databases/SQL Server#authentication)

## Limitations

- [See SQL Server](Supported Databases/SQL Server#limitations)
- The JTDS driver does not support Azure Synapse
