---
subtitle: HSQLDB
---
# HSQLDB

## Supported Versions

- `2.4`
- `2.3` {% include teams.html %}
- `2.2` {% include teams.html %}
- `2.0` {% include teams.html %}
- `1.8` {% include teams.html %}

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
<td><code>jdbc:hsqldb:file:<i>file</i></code></td>
</tr>
<tr>
<th>Ships with Flyway Command-line</th>
<td>Yes</td>
</tr>
<tr>
<th>Maven Central coordinates</th>
<td><code>org.hsqldb:hsqldb</code></td>
</tr>
<tr>
<th>Supported versions</th>
<td><code>1.8</code> and later</td>
</tr>
<tr>
<th>Default Java class</th>
<td><code>org.hsqldb.jdbcDriver</code></td>
</tr>
</table>

## SQL Script Syntax

- [Standard SQL syntax](Concepts/migrations#sql-based-migrations#syntax) with statement delimiter **;**
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
