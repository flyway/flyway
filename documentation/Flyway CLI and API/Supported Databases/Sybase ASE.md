---
subtitle: Sybase ASE
---
# Sybase ASE

## Supported Versions

- `16.3`
- `16.2` {% include teams.html %}
- `16.1` {% include teams.html %}
- `16.0` {% include teams.html %}
- `15.7` {% include teams.html %}

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
<thead>
<tr>
<th></th>
<th>jConnect</th>
<th>jTDS</th>
</tr>
</thead>
<tr>
<th>URL format</th>
<td><code>jdbc:sybase:Tds:<i>host</i>:<i>port</i>/<i>database</i></code></td>
<td><code>jdbc:jtds:sybase://<i>host</i>:<i>port</i>/<i>database</i></code></td>
</tr>
<tr>
<th>Ships with Flyway Command-line</th>
<td>No</td>
<td>Yes</td>
</tr>
<tr>
<th>Download</th>
<td>Download from <a href="https://sap.com">sap.com</a></td>
<td>Maven Central coordinates: <code>net.sourceforge.jtds:jtds</code></td>
</tr>
<tr>
<th>Supported versions</th>
<td><code>7.0</code> and later</td>
<td><code>1.3.1</code> and later</td>
</tr>
<tr>
<th>Default Java class</th>
<td><code>com.sybase.jdbc4.jdbc.SybDriver</code></td>
<td><code>net.sourceforge.jtds.jdbc.Driver</code></td>
</tr>
</table>

## SQL Script Syntax

- [Standard SQL syntax](Concepts/migrations#sql-based-migrations#syntax) with statement delimiter **GO**
- T-SQL

### Compatibility

- DDL exported by Sybase ASE Client can be used unchanged in a Flyway migration.
- Any Sybase ASE Server sql script executed by Flyway, can be executed by Sybase Interactive SQL client, Sybase Central and
        other Sybase ASE Server-compatible tools (after the placeholders have been replaced).

### Example

<pre class="prettyprint">/* Single line comment */
CREATE TABLE Customers (
CustomerId smallint identity(1,1),
Name nvarchar(255),
Priority tinyint
)
GO

CREATE TABLE Sales (
TransactionId smallint identity(1,1),
CustomerId smallint,
[Net Amount] int,
Completed bit
)
GO

/*
Multi-line
comment
*/
-- TSQL
CREATE TRIGGER Update_Customer on Sales
for insert,update
as
declare @errorMsg VARCHAR(200),
        @customerID VARCHAR(10)
BEGIN
    select @customerID = customerID from inserted

    IF exists (select 1 from Sales tbl, inserted i
        where tbl.customerID = i.customerID )
    begin
                select @errorMsg = 'Cannot have 2 record with the same customer ID '+@customerID
        	raiserror 99999 @errorMsg
        	rollback
    end
END

GO

-- Placeholder
INSERT INTO ${tableName} (name) VALUES ('Mr. T');</pre>

## Limitations

- No Support for <code>flyway.schemas</code> due to Sybase ASE limitations.
- No Support for DDL transactions due to Sybase ASE limitations.
