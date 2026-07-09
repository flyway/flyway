---
subtitle: OceanBase
---

- **Verified Versions:** `N/A`
- **Maintainer:** {% include community-db.html %}

## Supported Versions and Support Levels

{% include database-boilerplate.html %}

## Driver

Flyway's OceanBase implementation reuses significant parts of the [MySQL Database](<Database Driver Reference/MySQL>) parser, since OceanBase operates in MySQL-compatible mode.

| Item                                 | Details                                                                                                                    |
| ------------------------------------- | --------------------------------------------------------------------------------------------------------------------------- |
| **URL format**                       | <code>jdbc:oceanbase://<i>host</i>:<i>port</i>/<i>database</i></code>                                                      |
| **SSL support**                      | Not tested                                                                                                                 |
| **Ships with Flyway Command-line**   | No                                                                                                                         |
| **Maven Central coordinates**        | `com.oceanbase:oceanbase-client`                                                                                          |
| **Supported versions**               | `N/A`                                                                                                                     |
| **Default Java class**               | `com.oceanbase.jdbc.Driver`                                                                                               |
| **Flyway Community implementation**  | [flyway-community-db-support](https://github.com/flyway/flyway-community-db-support/tree/main/flyway-database-oceanbase) |

## Java Usage

OceanBase support is a separate dependency for Flyway and will need to be added to your Java project to access these features.

### Maven
#### Open Source
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-oceanbase</artifactId>
</dependency>
```

### Gradle
#### Open Source
```groovy
buildscript {
    dependencies {
        implementation "org.flywaydb:flyway-database-oceanbase"
    }
}
```

## SQL Script Syntax

- Standard SQL syntax with statement delimiter **;**
- MySQL-style single-line comments (`# Comment`)

### Example
```sql
/* Single line comment */
CREATE TABLE test_data (
 value VARCHAR(25) NOT NULL,
 PRIMARY KEY(value)
);

-- Placeholder
INSERT INTO ${tableName} (name) VALUES ('Mr. T');
```

## Notes

OceanBase is a MySQL-compatible database and Flyway usage is largely the same as for MySQL. For more details, please refer to the [MySQL](<Database Driver Reference/MySQL>) page.
