---
subtitle: TiDB
---
# TiDB (Titanium DB)

## Supported Versions

- `5.0`

## Support Level

<table class="table">
    <tr>
        <th width="25%">Compatible</th>
        <td>&#10003;</td>
    </tr>
    <tr>
        <th width="25%">Certified</th>
        <td>&#10060;</td>
    </tr>
    <tr>
        <th width="25%">Guaranteed</th>
        <td>&#10060;</td>
    </tr>
</table>

Support Level determines the degree of support available for this database ([learn more](Learn More/Database Support Levels)). 

## Drivers

<table class="table">
<thead>
<tr>
<th></th>
<th>MySQL</th>
</tr>
</thead>
<tr>
<th>URL format</th>
<td><code>jdbc:mysql://<i>host</i>:<i>port</i>/<i>database</i></code></td>
</tr>
<tr>
<th>SSL support</th>
<td>Not tested</td>
</tr>
<tr>
<th>Ships with Flyway Command-line</th>
<td>Yes</td>
</tr>
<tr>
<th>Maven Central coordinates</th>
<td><code>mysql:mysql-connector-java</code></td>
</tr>
<tr>
<th>Supported versions</th>
<td><code>5.0</code> and later</td>
</tr>
<tr>
<th>Default Java class</th>
<td><code>com.mysql.jdbc.Driver</code></td>
</tr>
</table>

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
