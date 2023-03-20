---
subtitle: SQL Server
---

# SQL Server

## Supported Versions

- `2022*`
- `2019`
- `2017`
- `2016` {% include teams.html %}
- `2014` {% include teams.html %}
- `2012` {% include enterprise.html %}
- `2008 R2` {% include enterprise.html %}
- `2008` {% include enterprise.html %}

*Tested with preview version (June 2022).

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

## Java Usage

SQLServer support is a separate dependency for Flyway and will need to be added to your Java project to access these features.

### Maven

#### Community

```xml

<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-sqlserver</artifactId>
</dependency>
```

#### Teams

```xml

<dependency>
    <groupId>org.flywaydb.enterprise</groupId>
    <artifactId>flyway-sqlserver</artifactId>
</dependency>
```

### Gradle

#### Community

```groovy
dependencies {
    compile "org.flywaydb:flyway-sqlserver"
}
```

#### Teams

```groovy
dependencies {
    compile "org.flywaydb.enterprise:flyway-sqlserver"
}
```

## SQL Script Syntax

- [Standard SQL syntax](Concepts/migrations#sql-based-migrations#syntax) with statement delimiter **GO**
- T-SQL

### Compatibility

- DDL exported by SQL Server can be used unchanged in a Flyway migration.
- Any SQL Server sql script executed by Flyway, can be executed by Sqlcmd, SQL Server Management Studio and other SQL Server-compatible tools (after the placeholders have been
  replaced).

### Example

```sql
/* Single line comment */
CREATE TABLE Customers (
CustomerId smallint identity(1,1),
Name nvarchar(255),
Priority tinyint
)
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
CREATE TRIGGER dbo.Update_Customer_Priority
 ON dbo.Sales
AFTER INSERT, UPDATE, DELETE
AS
WITH CTE AS (
 select CustomerId from inserted
 union
 select CustomerId from deleted
)
UPDATE Customers
SET
 Priority =
   case
     when t.Total &lt; 10000 then 3
     when t.Total between 10000 and 50000 then 2
     when t.Total &gt; 50000 then 1
     when t.Total IS NULL then NULL
   end
FROM Customers c
INNER JOIN CTE ON CTE.CustomerId = c.CustomerId
LEFT JOIN (
 select
   Sales.CustomerId,
   SUM([Net Amount]) Total
 from Sales
 inner join CTE on CTE.CustomerId = Sales.CustomerId
 where
   Completed = 1
 group by Sales.CustomerId
) t ON t.CustomerId = c.CustomerId
GO

-- Placeholder
INSERT INTO ${tableName} (name) VALUES ('Mr. T');
```

## Authentication

SQL Server supports several methods of authentication. These include:

- SQL Server Authentication
- Windows Authentication
- Azure Active Directory
- Kerberos {% include teams.html %}

SQL Server Authentication works 'out-of-the-box' with Flyway, whereas the others require extra manual setup.

The instructions provided here are adapted from
the [Microsoft JDBC Driver for SQL Server documentation](https://docs.microsoft.com/en-us/sql/connect/jdbc/microsoft-jdbc-driver-for-sql-server?view=sql-server-ver15). Refer to
this when troubleshooting authentication problems.

**Note:** These instructions may be incomplete. Flyway depends on Microsoft's JDBC drivers, which in turn have many environmental dependencies to enable different authentication
types. You may have to perform your own research to get the JDBC driver working for the different authentication types.

### SQL Server Authentication

This uses a straightforward username and password to authenticate. Provide these with the `user` and `password` configuration options.

### Windows Authentication

[Windows Authentication, also known as Integrated Security](https://docs.microsoft.com/en-us/dotnet/framework/data/adonet/sql/authentication-in-sql-server), is enabled by amending
your JDBC connection string to set `integratedSecurity=true`.

Syntax:<br/> `jdbc:sqlserver://<host>:<port>;databaseName=<dbname>;integratedSecurity=true`.

Example:<br/> `jdbc:sqlserver://server01:1234;databaseName=AdventureWorks;integratedSecurity=true`.

### Azure Active Directory

#### Installing MSAL4J

You must add Microsoft's [MSAL4J library](https://mvnrepository.com/artifact/com.microsoft.azure/msal4j) to your classpath. For instance, as a Maven or Gradle dependency.

For command-line users, MSAL4J is already included, so no extra installation is required.

#### Connecting

There are several types of Azure Active Directory authentication:

- Azure Active Directory with MFA
- Azure Active Directory Integrated
- Azure Active Directory MSI
- Azure Active Directory with Password
- Azure Active Directory Service Principal
- Access Tokens

To use the various authentication types, amend your JDBC URL to set the `authentication` parameter:

- For Active Directory Integrated set `authentication=ActiveDirectoryIntegrated`
    - e.g: <code>jdbc:sqlserver://<i>host</i>:<i>port</i>;databaseName=<i>database</i>;authentication=ActiveDirectoryIntegrated</code>
- For Active Directory MSI set `authentication=ActiveDirectoryMSI`
    - e.g: <code>jdbc:sqlserver://<i>host</i>:<i>port</i>;databaseName=<i>database</i>;authentication=ActiveDirectoryMSI</code>
- For Active Directory With Password set `authentication=ActiveDirectoryPassword`
    - e.g: <code>jdbc:sqlserver://<i>host</i>:<i>port</i>;databaseName=<i>database</i>;authentication=ActiveDirectoryPassword</code>
    - You must also supply a username and password with Flyway's `user` and `password` configuration options
- For Active Directory Interactive set `authentication=ActiveDirectoryInteractive`
    - e.g: <code>jdbc:sqlserver://<i>host</i>:<i>port</i>;databaseName=<i>database</i>;authentication=ActiveDirectoryInteractive</code>
    - This will begin an interactive process which expects user input (e.g. a dialogue box), so it's not recommended in automated environments
- For Active Directory Service Principal set `authentication=ActiveDirectoryServicePrincipal `
    - e.g: <code>jdbc:sqlserver://<i>host</i>:<i>port</i>;databaseName=<i>database</i>;authentication=ActiveDirectoryServicePrincipal</code>

[The Microsoft documentation has more details about how these work with JDBC URLs](https://docs.microsoft.com/en-us/sql/connect/jdbc/connecting-using-azure-active-directory-authentication?view=sql-server-ver15)
.

#### Azure access tokens

Another way to authenticate using Azure Active Directory is through access tokens. As of the time of writing, the access token property on Microsoft's JDBC driver cannot be
supplied through the URL. Therefore you should use Flyway's `jdbcProperties` configuration property.

E.g, in a `flyway.conf` file:

```
flyway.jdbcProperties.accessToken=my-access-token
```

This is equivalent to
the [process of setting `accessToken` as described on this Microsoft documentation page](https://docs.microsoft.com/en-us/sql/connect/jdbc/connecting-using-azure-active-directory-authentication?view=sql-server-ver15#connecting-using-access-token)
.

### Kerberos

{% include teams.html %}

Kerberos authentication can also be used to connect Flyway to your database.

To set this up, you will need to pass the path to your Kerberos configuration file to the parameter [`kerberosConfigFile`](Configuration/Parameters/Kerberos Config File) and the
path to your login module configuration file to the parameter [`plugin.sqlserver.kerberos.login.file`](Configuration/Parameters/SQL Server Kerberos Login File).

You may also need to add `;authenticationScheme=JavaKerberos` to your JDBC URL.

For more information on Kerberos authentication with SQL Server, you can read the official
documentation [here](https://docs.microsoft.com/en-us/sql/connect/jdbc/using-kerberos-integrated-authentication-to-connect-to-sql-server?view=sql-server-ver15).

## Connecting to a Named Instance

When connecting to a named instance, the JDBC URL must be of the form:

```
jdbc:sqlserver://<server_name>;instanceName=<instance_name>;databaseName=<database_name>
```

For example:

```
jdbc:sqlserver://test_server;instanceName=test_instance;databaseName=test_database
```

**Note:** If a named instance is used along with the `<host>:<port>` syntax in the JDBC URL, the driver will connect to the port over the named instance.

## Limitations

- Flyway's automatic detection for whether SQL statements are valid in transactions does not apply to
  `CREATE/ALTER/DROP` statements acting on memory-optimized tables (that is, those created with
  `WITH (MEMORY_OPTIMIZED = ON)`). You will need to override the `executeInTransaction` setting to be false,
  on a [per-script basis](Configuration/Script Config Files).
- SQL Server is unable to change the default schema for a session. Therefore, setting the `flyway.defaultSchema` property
  has no value, unless used for a [Placeholder](Concepts/migrations#placeholder-replacement) in
  your sql scripts. If you decide to use `flyway.defaultSchema`, it also must exist in `flyway.schemas`.
- By default, the flyway schema history table will try to write to the default schema for the database connection. You may
  specify which schema to write this table to by setting `flyway.schemas=custom_schema`, as the first entry will become the
  default schema if `flyway.defaultSchema` itself is not set.
- With these limitations in mind, please refer to the properties or options mentioned [here](Configuration/Parameters/Default Schema) for descriptions/consequences.
- If using the JTDS driver, then setting `ANSI_NULLS` or `QUOTED_IDENTIFIER` in a script will cause an error. This is
  a driver limitation, and can be solved by using the Microsoft driver instead.
- When running clean, no users will be dropped
