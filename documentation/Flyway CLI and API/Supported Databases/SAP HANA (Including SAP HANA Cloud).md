---
subtitle: SAP HANA
---
# SAP HANA (Including SAP HANA Cloud)

## Supported Versions - On-Premise

- `2.0`
- `1.0` {% include teams.html %}

## Supported Versions - SAP HANA Cloud

- `4.0`

## Support Level

<table class="table">
    <tr>
        <th width="25%">Compatible</th>
        <td>&#10003;</td>
    </tr>
    <tr>
        <th width="25%">Certified</th>
        <td>&#10003;</td>
    </tr>
    <tr>
        <th width="25%">Guaranteed</th>
        <td>&#10003; {% include teams.html %}</td>
    </tr>
</table>

Support Level determines the degree of support available for this database ([learn more](Learn More/Database Support Levels)). 

## Driver

<table class="table">
<tr>
<th>URL format</th>
<td><code>jdbc:sap://<i>host</i>:<i>port</i>/?databaseName=<i>database</i></code></td>
</tr>
<tr>
<th>Ships with Flyway Command-line</th>
<td>No</td>
</tr>
<tr>
<th>Maven Central coordinates</th>
<td><code>com.sap.cloud.db.jdbc:ngdbc</code></td>
</tr>
<tr>
<th>Supported versions</th>
<td><code>1.0</code> and later</td>
</tr>
<tr>
<th>Default Java class</th>
<td><code>com.sap.db.jdbc.Driver</code></td>
</tr>
</table>

## SQL Script Syntax

- [Standard SQL syntax](Concepts/migrations#sql-based-migrations#syntax) with statement delimiter **;**
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
