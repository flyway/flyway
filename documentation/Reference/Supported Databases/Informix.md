---
subtitle: Informix
---
# Informix
- **Verified Versions:** 12.10
- **Maintainer:** {% include redgate-badge.html %}

## Supported Versions and Support Levels

{% include database-boilerplate.html %}

## Driver

| Item                               | Details                                                                                      |
|------------------------------------|----------------------------------------------------------------------------------------------|
| **URL format**                     | <code>jdbc:informix-sqli://<i>host</i>:<i>port</i>/<i>database</i>:informixserver=dev</code> |
| **Ships with Flyway Command-line** | No                                                                                           |
| **Download**                       | Maven Central coordinates: `com.ibm.informix:jdbc`                                           |
| **Supported versions**             | `4.10.10.0` and later                                                                        |
| **Default Java class**             | `com.informix.jdbc.IfxDriver`                                                                |



## Java Usage
Informix support is a separate dependency for Flyway and will need to be added to your Java project to access these features.
Informix is found within the `flyway-database-informix` plugin module.
### Maven
#### Redgate
```xml
<dependency>
    <groupId>com.redgate.flyway</groupId>
    <artifactId>flyway-database-informix</artifactId>
</dependency>
```
#### Open Source
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-informix</artifactId>
</dependency>
```

### Gradle
#### Redgate
```groovy
dependencies {
    implementation "com.redgate.flyway:flyway-database-informix"
}
```
#### Open Source
```groovy
dependencies {
    implementation "org.flywaydb:flyway-database-informix"
}
```

## SQL Script Syntax

- Standard SQL syntax with statement delimiter **GO**
- SPL

### Compatibility

- Both Informix SQL and SPL statements can be used unchanged in a Flyway migration.

### Example

```sql
/* Single line comment */
CREATE SEQUENCE seq_2
   INCREMENT BY 1 START WITH 1
   MAXVALUE 30 MINVALUE 0
   NOCYCLE CACHE 10 ORDER;

CREATE TABLE tab1 (col1 int, col2 int);
INSERT INTO tab1 VALUES (0, 0);

INSERT INTO tab1 (col1, col2) VALUES (seq_2.NEXTVAL, seq_2.NEXTVAL);

/*
Multi-line
comment
*/
-- SPL
CREATE PROCEDURE raise_prices ( per_cent INT, selected_unit CHAR )
	UPDATE stock SET unit_price = unit_price + (unit_price * (per_cent/100) )
	where unit=selected_unit;
END PROCEDURE;

CREATE FUNCTION square ( num INT )
   RETURNING INT;
   return (num * num);
END FUNCTION
   DOCUMENT "USAGE: Update a price by a percentage",
         "Enter an integer percentage from 1 - 100",
         "and a part id number";

-- Placeholder
INSERT INTO ${tableName} (name) VALUES ('Mr. T');
```

## Limitations

- No Support for <code>flyway.schemas</code> due to Informix limitations.
- No Support for DDL transactions due to Informix limitations.
