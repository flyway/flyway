---
subtitle: Sybase ASE
---

- **Verified Versions:** 15.7, 16.3
- **Maintainer:** {% include redgate-badge.html %}

## Supported Versions and Support Levels

{% include database-boilerplate.html %}

## Drivers

| Item                               | jConnect                                                             | jTDS                                                                    |
|------------------------------------|----------------------------------------------------------------------|-------------------------------------------------------------------------|
| **URL format**                     | <code>jdbc:sybase:Tds:<i>host</i>:<i>port</i>/<i>database</i></code> | <code>jdbc:jtds:sybase://<i>host</i>:<i>port</i>/<i>database</i></code> |
| **Ships with Flyway Command-line** | No                                                                   | Yes                                                                     |
| **Download**                       | Download from [sap.com](https://sap.com)                             | Maven Central coordinates: `net.sourceforge.jtds:jtds`                  |
| **Supported versions**             | `7.0` and later                                                      | `1.3.1` and later                                                       |
| **Default Java class**             | `com.sybase.jdbc4.jdbc.SybDriver`                                    | `net.sourceforge.jtds.jdbc.Driver`                                      |


## Java Usage
Sybase ASE support is a separate dependency for Flyway and will need to be added to your Java project to access these features.
Sybase ASE is found within the `flyway-database-sybasease` plugin module.
### Maven
#### Redgate
```xml
<dependency>
    <groupId>com.redgate.flyway</groupId>
    <artifactId>flyway-database-sybasease</artifactId>
</dependency>
```
#### Open Source
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-sybasease</artifactId>
</dependency>
```

### Gradle
#### Redgate
```groovy
buildscript {
    dependencies {
        implementation "com.redgate.flyway:flyway-database-sybasease"
    }
}
```
#### Open Source
```groovy
buildscript {
    dependencies {
        implementation "org.flywaydb:flyway-database-sybasease"
    }
}
```


## SQL Script Syntax

- Standard SQL syntax with statement delimiter **GO**
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
