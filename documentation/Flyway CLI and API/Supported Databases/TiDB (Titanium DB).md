---
subtitle: TiDB
---
# TiDB (Titanium DB)
- **Verified Versions:** 5.0
- **Maintainer:** Community

## Supported Versions and Support Levels
For information regarding the supported version and support levels available,
please see [Supported Databases for Flyway](https://documentation.red-gate.com/flyway/learn-more-about-flyway/system-requirements/supported-databases-for-flyway)

For information regarding the Flyway features available, please see [Flyway feature glossary](https://documentation.red-gate.com/flyway/learn-more-about-flyway/feature-glossary)

## Driver

| Item                               | Details                                                           |
|------------------------------------|-------------------------------------------------------------------|
| **URL format**                     | <code>jdbc:mysql://<i>host</i>:<i>port</i>/<i>database</i></code> |
| **SSL support**                    | Not tested                                                        |
| **Ships with Flyway Command-line** | Yes                                                               |
| **Maven Central coordinates**      | `mysql:mysql-connector-java`                                      |
| **Supported versions**             | `5.0` and later                                                   |
| **Default Java class**             | `com.mysql.jdbc.Driver`                                           |


## Java Usage

YugabyteDB support is a separate dependency for Flyway and will need to be added to your Java project to access these features.

### Maven

#### Open Source

```xml

<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-tidb</artifactId>
</dependency>
```

### Gradle

#### Open Source

```groovy
dependencies {
    compile "org.flywaydb:flyway-database-tidb"
}
```

## SQL Script Syntax

- [Standard SQL syntax](Concepts/migrations#sql-based-migrations#syntax) with statement delimiter **;**
- MySQL-style single-line comments (# Comment)
 
### Example

```sql
/* Single line comment */
CREATE TABLE test_data (
 value VARCHAR(25) NOT NULL,
 PRIMARY KEY(value)
);

/*
Multi-line
comment
*/

-- MySQL procedure
DELIMITER //
CREATE PROCEDURE AddData()
 BEGIN
   # MySQL-style single line comment
   INSERT INTO test_data (value) VALUES ('Hello');
 END //
DELIMITER;

CALL AddData();

-- Placeholder
INSERT INTO ${tableName} (name) VALUES ('Mr. T');
```
