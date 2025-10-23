---
subtitle: SAP HANA
---

- **Verified Versions:** 
  - On-Premise: 1.0, 2.0
  - SAP HANA Cloud: 4.0
- **Maintainer:** {% include redgate-badge.html %}

## Supported Versions and Support Levels

{% include database-boilerplate.html %}

## Driver

| Item                               | Details                                                                       |
|------------------------------------|-------------------------------------------------------------------------------|
| **URL format**                     | <code>jdbc:sap://<i>host</i>:<i>port</i>/?databaseName=<i>database</i></code> |
| **Ships with Flyway Command-line** | No                                                                            |
| **Maven Central coordinates**      | `com.sap.cloud.db.jdbc:ngdbc`                                                 |
| **Supported versions**             | `1.0` and later                                                               |
| **Default Java class**             | `com.sap.db.jdbc.Driver`                                                      |

## Java Usage
SAP HANA support is a separate dependency for Flyway and will need to be added to your Java project to access these features.
SAP HANA is found within the `flyway-database-saphana` plugin module.

### Maven
#### Redgate
```xml
<dependency>
    <groupId>com.redgate.flyway</groupId>
    <artifactId>flyway-database-saphana</artifactId>
</dependency>
```
#### Open Source
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-saphana</artifactId>
</dependency>
```
### Gradle
#### Redgate
```groovy
buildscript {
    dependencies {
        implementation "com.redgate.flyway:flyway-database-saphana"
    }
}
```
#### Open Source
```groovy
buildscript {
    dependencies {
        implementation "org.flywaydb:flyway-database-saphana"
    }
}
```

## SQL Script Syntax

- Standard SQL syntax with statement delimiter **;**
- `BEGIN .. END;` blocks used for triggers and anonymous do blocks

### Compatibility

- SAP HANA DDL can be used unchanged in a Flyway migration.
- Any SAP HANA sql script executed by Flyway, can be executed by SAP HANA (after the placeholders have been replaced).

### Example

<pre class="prettyprint">/* Single line comment */
CREATE TABLE t (a INT, b NVARCHAR(10), c NVARCHAR(20));
CREATE INDEX idx ON t(b);

CREATE COLUMN TABLE A (A VARCHAR(10) PRIMARY KEY, B VARCHAR(10));
CREATE FULLTEXT INDEX i ON A(A) FUZZY SEARCH INDEX OFF SYNC;

/*
Multi-line
comment
*/

CREATE TABLE TARGET ( A INT);
CREATE TABLE control_tab(id INT PRIMARY KEY, name VARCHAR(30), payment INT);
CREATE TABLE message_box(message VARCHAR(200), log_time TIMESTAMP);

-- Triggers with complex BEGIN END; blocks
CREATE TRIGGER TEST_TRIGGER_FOR_INSERT
AFTER INSERT ON TARGET
BEGIN
 DECLARE v_id        INT := 0;
 DECLARE v_name      VARCHAR(20) := '';
 DECLARE v_pay       INT := 0;
 DECLARE v_msg       VARCHAR(200) := '';
 DELETE FROM message_box;
 FOR v_id IN 100 .. 103 DO
     SELECT name, payment INTO v_name, v_pay FROM control_tab WHERE id = :v_id;
     v_msg := :v_name || ' has ' || TO_CHAR(:v_pay);
     INSERT INTO message_box VALUES (:v_msg, CURRENT_TIMESTAMP);
 END FOR;
END;

CREATE TABLE SAMPLE ( A INT);
CREATE TRIGGER TEST_TRIGGER_WHILE_UPDATE
AFTER UPDATE ON TARGET
BEGIN
 DECLARE found INT := 1;
 DECLARE val INT := 1;
 WHILE :found <> 0 DO
     SELECT count(*) INTO found FROM sample WHERE a = :val;
     IF :found = 0 THEN
         INSERT INTO sample VALUES(:val);
     END IF;
     val := :val + 1;
 END WHILE;
END;

-- Anonymous blocks
DO
BEGIN
    DECLARE v_count INT;
    CREATE TABLE TAB1 (I INTEGER); 
    FOR v_count IN 1..10 DO
        INSERT INTO TAB1 VALUES (:v_count);
    END FOR;
END;

-- Placeholders
INSERT INTO ${tableName} (name) VALUES ('Mr. T');</pre>

## Limitations

- *None*
