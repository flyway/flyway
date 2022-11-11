---
layout: documentation
menu: tidb
subtitle: TiDB
---
# TiDB (Titanium DB)

## Supported Versions

- `5.0` {% include teams.html %}

## Support Level

<table class="table">
    <tr>
        <th width="25%">Compatible</th>
        <td>✅</td>
    </tr>
    <tr>
        <th width="25%">Certified</th>
        <td>❌</td>
    </tr>
    <tr>
        <th width="25%">Guaranteed</th>
        <td>❌ {% include teams.html %}</td>
    </tr>
</table>

Support Level determines the degree of support available for this database ([learn more](/documentation/learnmore/database-support)). 

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
<td><code>mysql:mysql-connector-java:8.0.12</code></td>
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

- [Standard SQL syntax](/documentation/concepts/migrations#sql-based-migrations#syntax) with statement delimiter **;**
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

<p class="next-steps">
    <a class="btn btn-primary" href="/documentation/database/testcontainers">TestContainers <i class="fa fa-arrow-right"></i></a>
</p>
