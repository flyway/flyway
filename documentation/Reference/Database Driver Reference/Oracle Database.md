---
subtitle: Oracle
---

- **Verified Versions:** 11.1, 21
- **Maintainer:** {% include redgate-badge.html %}

All editions are supported, including XE.

## Supported Versions and Support Levels

{% include database-boilerplate.html %}

## Driver

| Item                               | Details                                                                      |
| ---------------------------------- | ---------------------------------------------------------------------------- |
| **URL format**                     | `jdbc:oracle:thin:@//host:port/service` <br> `jdbc:oracle:thin:@tns_entry` * |
| **Ships with Flyway Command-line** | Yes                                                                          |
| **Maven Central coordinates**      | `com.oracle.database.jdbc:ojdbc11`                                           |
| **Supported versions**             | Oracle Database versions - 21c, 19c, 18c, and 12.2                           |
| **Default Java class**             | `oracle.jdbc.OracleDriver`                                                   |

\* `TNS_ADMIN` environment variable must point to the directory of where `tnsnames.ora` resides

## Related database-specific configuration

Oracle-specific configuration can be found [here](<Configuration/Flyway Namespace/Flyway Oracle Namespace>).

## Java Usage

Oracle support is a separate dependency for Flyway and will need to be added to your Java project to access these features.

### Maven

#### Redgate

```xml

<dependency>
    <groupId>com.redgate.flyway</groupId>
    <artifactId>flyway-database-oracle</artifactId>
</dependency>
```

#### Open Source

```xml

<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-oracle</artifactId>
</dependency>
```

### Gradle

#### Redgate

```groovy
dependencies {
    implementation "com.redgate.flyway:flyway-database-oracle"
}
```

#### Open Source

```groovy
dependencies {
    implementation "org.flywaydb:flyway-database-oracle"
}
```

## SQL Script Syntax

- Standard SQL statements ending with the semicolon (`;`) delimiter are handled as normal.
- PL/SQL blocks and SQL*Plus statements must be delimited by `/` on a new line. PL/SQL includes:
    - Types
    - Packages
    - Functions
    - Procedures
    - Views with functions/procedures
    - Triggers
    - Java sources

### Compatibility

- DDL exported by Oracle can be used unchanged in a Flyway migration
- Any Oracle SQL script executed by Flyway can be executed by SQL*Plus and other Oracle-compatible tools (after the placeholders have been replaced)

### Example

<pre class="prettyprint">/* Single line comment */
CREATE TABLE test_user (
  name VARCHAR(25) NOT NULL,
  PRIMARY KEY(name)
);

/*
Multi-line
comment
*/
-- PL/SQL block
CREATE TRIGGER test_trig AFTER insert ON test_user
BEGIN
   UPDATE test_user SET name = CONCAT(name, ' triggered');
END;
/

-- Placeholder
INSERT INTO ${tableName} (name) VALUES ('Mr. T');</pre>

## SQL*Plus commands

{% include teams.html %}

In addition to the regular Oracle SQL syntax, Flyway Teams also comes with support for many Oracle SQL*Plus commands.

This support is disabled by default and must be activated using the [`oracle.sqlplus`](<Configuration/Flyway Namespace/Flyway Oracle Namespace/Flyway Oracle SQLPlus Setting>) flag.

The SQL\*Plus capability within Flyway is a re-implementation so may not behave exactly as native SQL\*Plus does.
If a feature you're looking for doesn't work as expected then we recommend using [script migrations](https://documentation.red-gate.com/flyway/flyway-concepts/migrations/script-migrations) to invoke the SQL*Plus command-line tool.

### Supported commands

The following commands are fully supported and can be used just like any regular command within your SQL migrations:

- `@` (only files, no URLs)
- `@@` (only files, no URLs)
- `DEFINE`
- `EXECUTE`
- `PROMPT`
- `REMARK`
- `SET DEFINE`
- `SET ECHO`
- `SET ESCAPE`
- `SET FEEDBACK`
- `SET FLAGGER`
- `SET HEADING`
- `SET LINESIZE` (DBMS_OUTPUT only)
- `SET NULL`
- `SET SCAN`
- `SET SERVEROUTPUT`
- `SET SUFFIX`
- `SET TERMOUT`
- `SET TIME`
- `SET TIMING`
- `SET VERIFY`
- `SHOW CON_ID`
- `SHOW DEFINE`
- `SHOW ECHO`
- `SHOW EDITION`
- `SHOW ERRORS`
- `SHOW ESCAPE`
- `SHOW FEEDBACK`
- `SHOW HEADING`
- `SHOW LINESIZE`
- `SHOW NULL`
- `SHOW RELEASE`
- `SHOW SCAN`
- `SHOW SERVEROUTPUT`
- `SHOW SUFFIX`
- `SHOW TERMOUT`
- `SHOW TIME`
- `SHOW TIMING`
- `SHOW USER`
- `SHOW VERIFY`
- `SPOOL`
- `START` (only files, no URLs)
- `UNDEFINE`
- `WHENEVER SQLERROR CONTINUE`
- `WHENEVER SQLERROR EXIT`
- `WHENEVER SQLERROR EXIT FAILURE`
- `WHENEVER SQLERROR EXIT SQL.SQLCODE`

The short form of these commands is also supported.

### Site Profiles (`glogin.sql`) & User Profiles (`login.sql`)

This feature allows you to set up your SQL\*Plus environment to use the same settings with each session. It allows you to execute statements before every script run, and is typically used to configure
the session in a consistent manner by calling SQL*Plus commands such as `SET FEEDBACK` and `SET DEFINE`.

Flyway will look for `login.sql` in all the valid migration locations, and load it if present. `glogin.sql` will be loaded from `$ORACLE_HOME/sqlplus/admin/glogin.sql` in UNIX, and `ORACLE_HOME\sqlplus\admin\glogin.sql` otherwise.

Profiles are only loaded when [`oracle.sqlplus`](<Configuration/Flyway Namespace/Flyway Oracle Namespace/Flyway Oracle SQLPlus Setting>) is enabled.

### Output

When `SET SERVEROUTPUT ON` is invoked, output produced by `DBMS_OUTPUT.PUT_LINE` will be shown in the console.

### Variable substitution

By default SQL\*Plus variable substitution support is enabled. `&VAR`-style variables will automatically be replaced
with the matching value supplied by either Flyway's regular placeholder configuration or a `DEFINE` command.
Use of these variables can be disabled in the usual way using the `SET DEFINE OFF` command.

While SQL\*Plus is interactive and will prompt for missing variable values, Flyway does not; it is an error not
to provide a value for all variables that need to be substituted.

Statements which contain a `&VAR`-style expression which is not intended to be substituted, such as in a
literal string, will either require `SET DEFINE OFF` beforehand, or some alternative construct to avoid use of
the ampersand.

For more information, see the [SQL\*Plus documentation](https://docs.oracle.com/en/database/oracle/oracle-database/21/sqpug/using-substitution-variables-sqlplus.html).

## Authentication

### JDBC

Oracle supports user and password being provided in the JDBC URL, in the form

`jdbc:oracle:thin:<user>/<password>@//<host>:<port>/<database>`

In this case, they do not need to be passed separately in configuration and the Flyway commandline will not prompt for them.

### Oracle Wallet

{% include teams.html %}

Flyway can connect to your databases using credentials in your Oracle Wallet.

First you need to ensure you have set the environment variable `TNS_ADMIN` to point to the location containing your `tnsnames.ora` file. Then you will need to configure the [`flyway.oracle.walletLocation`](<Configuration/Flyway Namespace/Flyway Oracle Namespace/Flyway Oracle Wallet Location Setting>) parameter to point to the location of your Oracle wallet. Lastly your URL should be provided as specified in `tnsnames.ora` i.e. if it is using an alias then connect with the `jdbc:oracle:thin:@db_alias` syntax.

With that configuration you will be able to connect to your database without providing any credentials in config.

### Kerberos

{% include teams.html %}

You can authenticate using Kerberos by specifying the location of the local Kerberos configuration file (which contains
details such as the locations of Kerberos Key Distribution Centers), and optionally the local credential cache, to
Flyway. For example, in `flyway.conf`:

```properties
flyway.oracle.kerberosConfigFile=/etc/krb5.conf
flyway.oracle.kerberosCacheFile=/tmp/krb5cc_123
```

### Proxy Authentication

{% include teams.html %}

Flyway allows you to proxy through other users during migrations. You can read about how to enable proxy authentication for users [here](https://docs.oracle.com/cd/E11882_01/java.112/e16548/proxya.htm#JJDBC28352).

To configure Flyway to use a proxy connection, you need to add to [`jdbcProperties`](<Configuration/Environments Namespace/Environment JDBC Properties Namespace>) a key `PROXY_USER_NAME` whose value is the name of the user you are trying to proxy as. For example, if you connect as user `A` to Flyway (i.e. `flyway.user=A`) and you want to proxy as user `B` for migrations, you need to add `flyway.jdbcProperties.PROXY_USER_NAME=B`.

## Limitations

- SPATIAL EXTENSIONS: `sdo_geom_metadata` can only be cleaned for the user currently logged in

### SQL*Plus

#### Unsupported commands

Not all SQL*Plus commands are supported by Flyway. Unsupported commands are gracefully ignored with a warning message.

#### Behavior parity

As much as possible, Flyway aims to emulate the behavior of the SQL*Plus client in Oracle SQL Developer. However, there are some edge cases where Flyway isn't able to emulate the behavior exactly. Known cases are detailed below:

- Abbreviations: Flyway is limited by JDBC support for particular commands, and this is more strict than the
  SQL*Plus client; in general abbreviations are supported by Flyway as documented [here](https://docs.oracle.com/cd/B19306_01/server.102/b14357/ch12041.htm),
  so for example `SHOW ERRORS` can be abbreviated to `SHO ERR`, but not `SHOW ERROR` (which is accepted by the client).

- SQL*Plus is known to replace CRLF pairs in string literals with a single LF. Flyway will not do this - instead it preserves scripts as they are written

If you encounter a discrepancy between the Oracle SQL*Plus client and Flyway, let us know via the official support email.

#### Referenced scripts and checksums

Flyway includes any referenced scripts when calculating checksums. This also extends to `login.sql` and `glogin.sql` since their contents can affect the reproducibility of a migration and can differ in different environments.

### Known issues and workarounds

Implementing a compatible solution to some problems isn't always possible, so we document those problems and the valid workarounds.

#### A default schema different to the current user's causes remote links to fail

Flyway alters the current schema to the specified [default schema](<Configuration/Flyway Namespace/Flyway Default Schema Setting>)as this is where the schema history table should reside. This causes remote links to fail in migrations that expect the current schema to be the user's. The workarounds for this are:

- Create the remote link via dynamic SQL in a stored procedure that resides in the correct schema. Stored procedures execute as the schema owner, so the remote link is created in the correct schema
- Use [beforeEachMigrate](<Callback Events>) and [afterEachMigrate](<Callback Events>) callbacks to alter the current schema as needed

### Debugging Oracle 
It is possible to enable debugging for the Oracle JDBC driver which may be of help if you want to get detailed information about what the driver is doing and even the individual SQL statements it is dealing with.

The process is covered in Oracle's [Diagnosability in JDBC](https://docs.oracle.com/en/database/oracle/oracle-database/21/jjdbc/JDBC-diagnosability.html) and we've done as much of the work as possible without impacting core Flyway usage.

The steps are:
1. Exchange the JDBC driver Flyway ships with for the equivalent debug version.
1. Provide an `assets/logging.properties` file to configure the detail in the logs you want.
1. Put things back after debugging.

Some things to bear in mind are:
- This could include all of your SQL statements in the output.
- It will work more slowly than using the driver Flyway ships with.
- You can potentially end up with a very lengthy log output (depends on the level you specify).
- We recommend logging to a file, if you log to the console and use Flyway Desktop on the same machine then the integration may break.  

We suggest that you only use this temporarily to debug a problem and you don't persist the generated logs.

#### Exchanging the JDBC driver
- Look in the `/drivers` folder of your Flyway engine installation and note the name of the file starting ojdbc11 (so something like `ojdbc11-21.18.0.0.jar`).
- Rename the extension from `.jar` to something else (like `.old`) so Java doesn't pick it up anymore but it will make it easier for you to put things back later.
- Download the equivalent debug jar file version, it will have the same name but include an `_g` in the filename (so something like `ojdbc11_g-21.18.0.0.jar`). It will go into the `/drivers` folder.

You can get this from either of these locations:
   - [Download from Maven Central.](https://central.sonatype.com/artifact/com.oracle.database.jdbc.debug/ojdbc11_g)
   - [Download from Oracle.](https://www.oracle.com/database/technologies/appdev/jdbc-drivers-archive.html) - you will need to extract the debug .jar file from the Zipped diagnosability Jars for ojdbc11 - `ojdbc11-diag.tar.gz`

#### Configuring logging
You will need to create a text file called `logging.properties` in the flyway `assets/` folder.
You can find details on how to configure the file in Oracle's [Diagnosability in JDBC.](https://docs.oracle.com/en/database/oracle/oracle-database/21/jjdbc/JDBC-diagnosability.html) 

An example `logging.properties` you can use to get started is as follows:
```ini
.level=SEVERE
oracle.jdbc.level=FINE
oracle.jdbc.handlers=java.util.logging.FileHandler
java.util.logging.FileHandler.level=FINE
java.util.logging.FileHandler.pattern=%t/jdbc.log
java.util.logging.FileHandler.formatter=java.util.logging.SimpleFormatter
```
This will put your logs in a file (pattern) called `jdbc.log` in your system temporary folder (`/tmp`, `/var/tmp` or  `C:\TEMP\` typically).

#### Put things back together
You can rename the original and debug Oracle JDBC driver files in the `drivers\` folder:
1. `ojdbc11_g-21.18.0.0.jar` -> `ojdbc11_g-21.18.0.0.old` (now Flyway won't find the debug driver anymore)
1. `ojdbc11-21.18.0.0.old` -> `ojdbc11-21.18.0.0.jar` (now Flyway will find the production driver again)
