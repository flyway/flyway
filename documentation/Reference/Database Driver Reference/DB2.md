---
subtitle: DB2
---

- **Verified Versions:** 9.7, 11.5
- **Maintainer:** {% include redgate-badge.html %}
### Supported Variants
- LUW

## Supported Versions and Support Levels

{% include database-boilerplate.html %}

## Driver

| Item                               | Details                                                                                                                                      |
|------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------|
| **URL format**                     | <code>jdbc:db2://<i>host</i>:<i>port</i>/<i>database</i></code>                                                                              |
| **Ships with Flyway Command-line** | No                                                                                                                                           |
| **Maven Central coordinates**      | `com.ibm.db2.jcc`                                                                                                                            |
| **Supported versions**             | `4.16.53` and later. For versions prior to 11.5, follow instructions on [ibm.com](http://www-01.ibm.com/support/docview.wss?uid=swg21363866) |
| **Default Java class**             | `com.ibm.db2.jcc.DB2Driver`                                                                                                                  |


## Java Usage
DB2 support is a separate dependency for Flyway and will need to be added to your Java project to access these features.
DB2 is found within the `flyway-database-db2` plugin module.
### Maven
#### Redgate
```xml
<dependency>
    <groupId>com.redgate.flyway</groupId>
    <artifactId>flyway-database-db2</artifactId>
</dependency>
```
#### Open Source
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-db2</artifactId>
</dependency>
```

### Gradle
#### Redgate
```groovy
buildscript {
    dependencies {
        implementation "com.redgate.flyway:flyway-database-db2"
    }
}
```
#### Open Source
```groovy
buildscript {
    dependencies {
        implementation "org.flywaydb:flyway-database-db2"
    }
}
```

## SQL Script Syntax

- Standard SQL syntax
- DB2 SQL-PL
- Terminator changes

### Compatibility

- DDL exported by DB2 can be used unchanged in a Flyway migration
- Any DB2 SQL script executed by Flyway, can be executed by db2 (after the placeholders have been replaced).

### Example

```sql
/* Single line comment */
CREATE TABLE test_user (
 name VARCHAR(25) NOT NULL,
 PRIMARY KEY(name)
);

/*
Multi-line
comment
*/

-- Placeholder
INSERT INTO ${tableName} (name) VALUES ('Mr. T');

-- SQL-PL
CREATE TRIGGER uniqueidx_trigger BEFORE INSERT ON usertable
	REFERENCING NEW ROW AS newrow
    FOR EACH ROW WHEN (newrow.name is not null)
	BEGIN ATOMIC
      IF EXISTS (SELECT * FROM usertable WHERE usertable.name = newrow.name) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'duplicate name';
      END IF;
    END;

-- Terminator changes
--#SET TERMINATOR @
CREATE FUNCTION TEST_FUNC(PARAM1 INTEGER, PARAM2 INTEGER)
  RETURNS INTEGER
LANGUAGE SQL
  RETURN
  1@   
--#SET TERMINATOR ;
CREATE FUNCTION TEST_FUNC(PARAM1 INTEGER, PARAM2 INTEGER, PARAM3 INTEGER)
  RETURNS INTEGER
LANGUAGE SQL
  RETURN
  1;
```
 
## Limitations

- Flyway doesn't currently support the use of DB2 utility commands [CLP & CLPPlus](https://www.ibm.com/docs/en/db2/10.1.0?topic=clp-command-line-processor-features) within SQL migrations. These utilities will need to accessed through Script migrations or Script [Callbacks](/callback-events).