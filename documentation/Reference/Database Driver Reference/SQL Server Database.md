---
subtitle: SQL Server
---

- **Verified Versions:** 2008, 2022
- **Maintainer:** {% include redgate-badge.html %}

## Supported Versions and Support Levels

{% include database-boilerplate.html %}

## Driver

| Item                               | Details                                                                                                                              |
|------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------|
| **URL format**                     | <code>jdbc:sqlserver://<i>host</i>:<i>port</i>;databaseName=<i>database</i></code>                                                   |
| **SSL support**                    | [Yes](https://docs.microsoft.com/en-us/sql/connect/jdbc/connecting-with-ssl-encryption?view=sql-server-ver15) \- add `;encrypt=true` |
| **Ships with Flyway Command-line** | Yes                                                                                                                                  |
| **Maven Central coordinates**      | `com.microsoft.sqlserver:mssql-jdbc`                                                                                                 |
| **Supported versions**             | `10.0` and later                                                                                                                      |
| **Default Java class**             | `com.microsoft.sqlserver.jdbc.SQLServerDriver`                                                                                       |

## Configuration

SQL-Server-specific configuration can be found [here](<Configuration/Flyway Namespace/Flyway SQL Server Namespace>).

## Java Usage

SQL Server support is a separate dependency for Flyway and will need to be added to your Java project to access these features.

### Maven

#### Redgate

```xml

<dependency>
    <groupId>com.redgate.flyway</groupId>
    <artifactId>flyway-sqlserver</artifactId>
</dependency>
```

#### Open Source

```xml

<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-sqlserver</artifactId>
</dependency>
```

### Gradle

#### Redgate

```groovy
dependencies {
    implementation "com.redgate.flyway:flyway-sqlserver"
}
```

#### Open Source

```groovy
dependencies {
    implementation "org.flywaydb:flyway-sqlserver"
}
```

## SQL Script Syntax

- Standard SQL syntax with statement delimiter **GO**
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
- Microsoft Entra
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

### Microsoft Entra

#### Installing Dependencies

You must add Microsoft's [MSAL4J library](https://mvnrepository.com/artifact/com.microsoft.azure/msal4j) to your classpath. For instance, as a Maven or Gradle dependency.
For Microsoft Entra MSI, Azure Identity is also required to be added to your classpath.

For command-line users, MSAL4J and Azure Identity are already included, so no extra installation is required.

#### Connecting

There are several types of Microsoft Entra authentication:

- Microsoft Entra with MFA
- Microsoft Entra Integrated
- Microsoft Entra MSI
- Microsoft Entra with Password
- Microsoft Entra Service Principal
- Access Tokens

To use the various authentication types, amend your JDBC URL to set the `authentication` parameter:

- For Microsoft Entra Integrated set `authentication=ActiveDirectoryIntegrated`
    - e.g: <code>jdbc:sqlserver://<i>host</i>:<i>port</i>;databaseName=<i>database</i>;authentication=ActiveDirectoryIntegrated</code>
- For Microsoft Entra MSI set `authentication=ActiveDirectoryMSI`
    - e.g: <code>jdbc:sqlserver://<i>host</i>:<i>port</i>;databaseName=<i>database</i>;authentication=ActiveDirectoryMSI</code>
- For Microsoft Entra With Password set `authentication=ActiveDirectoryPassword`
    - e.g: <code>jdbc:sqlserver://<i>host</i>:<i>port</i>;databaseName=<i>database</i>;authentication=ActiveDirectoryPassword</code>
    - You must also supply a username and password with Flyway's `user` and `password` configuration options
- For Microsoft Entra Interactive set `authentication=ActiveDirectoryInteractive`
    - e.g: <code>jdbc:sqlserver://<i>host</i>:<i>port</i>;databaseName=<i>database</i>;authentication=ActiveDirectoryInteractive</code>
    - This will begin an interactive process which expects user input (e.g. a dialogue box), so it's not recommended in automated environments
- For Microsoft Entra Service Principal set `authentication=ActiveDirectoryServicePrincipal `
    - e.g: <code>jdbc:sqlserver://<i>host</i>:<i>port</i>;databaseName=<i>database</i>;authentication=ActiveDirectoryServicePrincipal</code>

[The Microsoft documentation has more details about how these work with JDBC URLs](https://docs.microsoft.com/en-us/sql/connect/jdbc/connecting-using-azure-active-directory-authentication?view=sql-server-ver15)
.

#### Azure access tokens

Another way to authenticate using Microsoft Entra is through access tokens. As of the time of writing, the access token property on Microsoft's JDBC driver can't be
supplied through the URL. You should use Flyway's `jdbcProperties` configuration property instead.

E.g, in a `flyway.toml` file:

```
[environments.default]
jdbcProperties.accessToken="my-access-token"
```

This is equivalent to
the [process of setting `accessToken` as described on this Microsoft documentation page](https://docs.microsoft.com/en-us/sql/connect/jdbc/connecting-using-azure-active-directory-authentication?view=sql-server-ver15#connecting-using-access-token)
.

### Kerberos

{% include teams.html %}

Kerberos authentication can also be used to connect Flyway to your database.

To set this up, you will need to pass the path to your Kerberos configuration file to the parameter [`kerberosConfigFile`](<Configuration/Flyway Namespace/Flyway Kerberos Config File Setting>)and the path to your login module configuration file to the parameter [`sqlserver.kerberos.login.file`](<Configuration/Flyway Namespace/Flyway SQL Server Namespace/Flyway SQL Server Kerberos Login File Setting>).

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
  on a [per-script basis](https://documentation.red-gate.com/flyway/flyway-concepts/migrations/migration-transaction-handling).
- SQL Server is unable to change the default schema for a session. Therefore, setting the `flyway.defaultSchema` property
  has no value, unless used for a [Placeholder](https://documentation.red-gate.com/flyway/flyway-concepts/migrations/migration-placeholders) in
  your sql scripts. If you decide to use `flyway.defaultSchema`, it also must exist in `flyway.schemas`.
- By default, the flyway schema history table will try to write to the default schema for the database connection. You may
  specify which schema to write this table to by setting `flyway.schemas=custom_schema`, as the first entry will become the
  default schema if `flyway.defaultSchema` itself is not set.
- With these limitations in mind, please refer to the properties or options mentioned [here](<Configuration/Flyway Namespace/Flyway Default Schema Setting>)for descriptions/consequences.
- If using the JTDS driver, then setting `ANSI_NULLS` or `QUOTED_IDENTIFIER` in a script will cause an error. This is
  a driver limitation, and can be solved by using the Microsoft driver instead.
- When running clean, no users will be dropped
