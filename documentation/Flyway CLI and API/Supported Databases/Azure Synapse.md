---
subtitle: Azure Synapse
---
# Azure Synapse

## Supported Versions

- `Latest`

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
<td><code>jdbc:sqlserver://<i>host</i>:<i>port</i>;databaseName=<i>database</i></code></td>
</tr>
<tr>
<th>SSL support</th>
<td><a href="https://docs.microsoft.com/en-us/sql/connect/jdbc/connecting-with-ssl-encryption?view=sql-server-ver15">Yes</a> - add <code>;encrypt=true</code></td>
</tr>
<tr>
<th>Ships with Flyway Command-line</th>
<td>Yes</td>
</tr>
<tr>
<th>Maven Central coordinates</th>
<td><code>com.microsoft.sqlserver:mssql-jdbc</code></td>
</tr>
<tr>
<th>Supported versions</th>
<td><code>4.0</code> and later</td>
</tr>
<tr>
<th>Default Java class</th>
<td><code>com.microsoft.sqlserver.jdbc.SQLServerDriver</code></td>
</tr>
</table>

## Azure Synapse Syntax

- [See SQL Server](Supported Databases/SQL Server#sql-server-syntax)

### Compatibility

- [See SQL Server](Supported Databases/SQL Server#compatibility)

### Example

```sql
/* Single line comment */
CREATE TABLE test_user (
  id INT NOT NULL,
  name VARCHAR(25) NOT NULL,  -- this is a valid ' comment
  PRIMARY KEY NONCLUSTERED (name) NOT ENFORCED
);
GO

-- Placeholder
INSERT INTO ${tableName} (name) VALUES ('Mr. T');
```

## Authentication

[See SQL Server](Supported Databases/SQL Server#authentication)

## Limitations

- [See SQL Server](Supported Databases/SQL Server#limitations)
- The JTDS driver does not support Azure Synapse
