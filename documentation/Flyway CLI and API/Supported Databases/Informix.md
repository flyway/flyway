---
subtitle: Informix
---
# Informix

## Supported Versions

- `12.10`

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

## Drivers

<table class="table">
<tr>
<th>URL format</th>
<td><code>jdbc:informix-sqli://<i>host</i>:<i>port</i>/<i>database</i>:informixserver=dev</code></td>
</tr>
<tr>
<th>Ships with Flyway Command-line</th>
<td>No</td>
</tr>
<tr>
<th>Download</th>
<td>Maven Central coordinates: <code>com.ibm.informix:jdbc</code></td>
</tr>
<tr>
<th>Supported versions</th>
<td><code>4.10.10.0</code> and later</td>
</tr>
<tr>
<th>Default Java class</th>
<td><code>com.informix.jdbc.IfxDriver</code></td>
</tr>
</table>

## SQL Script Syntax

- [Standard SQL syntax](Concepts/migrations#sql-based-migrations#syntax) with statement delimiter **GO**
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
