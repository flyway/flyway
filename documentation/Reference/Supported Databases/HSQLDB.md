---
subtitle: HSQLDB
---
# HSQLDB
- **Verified Versions:** 2.7
- **Maintainer:** {% include redgate-badge.html %}

## Supported Versions and Support Levels

{% include database-boilerplate.html %}

## Driver

| Item                               | Details                                   |
|------------------------------------|-------------------------------------------|
| **URL format**                     | <code>jdbc:hsqldb:file:<i>file</i></code> |
| **Ships with Flyway Command-line** | Yes                                       |
| **Maven Central coordinates**      | `org.hsqldb:hsqldb`                       |
| **Supported versions**             | `1.8` and later                           |
| **Default Java class**             | `org.hsqldb.jdbcDriver`                   |


## Java Usage
HSQLDB support is a separate dependency for Flyway and will need to be added to your Java project to access these features.
HSQLDB is found within the `flyway-database-hsqldb` plugin module.
### Maven
#### Redgate
```xml
<dependency>
    <groupId>com.redgate.flyway</groupId>
    <artifactId>flyway-database-hsqldb</artifactId>
</dependency>
```
#### Open source
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-hsqldb</artifactId>
</dependency>
```

### Gradle
#### Redgate
```groovy
dependencies {
    implementation "com.redgate.flyway:flyway-database-hsqldb"
}
```
#### Open source
```groovy
dependencies {
    implementation "org.flywaydb:flyway-database-hsqldb"
}
```

## SQL Script Syntax

- Standard SQL syntax with statement delimiter **;**
- Triggers with `BEGIN ATOMIC ... END;` block

### Compatibility
    
- DDL exported by HSQLDB can be used unchanged in a Flyway migration
- Any HSQLDB SQL script executed by Flyway, can be executed by the Hsql tools (after the placeholders have been replaced)

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

-- Sql-style comment

-- Placeholder
INSERT INTO ${tableName} (name) VALUES ('Mr. T');

CREATE TRIGGER uniqueidx_trigger BEFORE INSERT ON usertable
	REFERENCING NEW ROW AS newrow
    FOR EACH ROW WHEN (newrow.name is not null)
	BEGIN ATOMIC
      IF EXISTS (SELECT * FROM usertable WHERE usertable.name = newrow.name) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'duplicate name';
      END IF;
    END;
```

## Limitations

- No concurrent migration support (to make Flyway cluster-safe) with HSQLDB 1.8, as this version does not properly support `SELECT ... FOR UPDATE` locking
