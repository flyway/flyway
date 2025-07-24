---
subtitle: Oracle - Native Connectors
---

- **Status:** Preview
- **Verified Versions:** 11.1, 21
- **Maintainer:** {% include redgate-badge.html %}
- **Edition:** Redgate

All Oracle editions are supported, including XE.

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

## Using Flyway with Oracle Native Connectors

Flyway's Native Connector support requires `SQLcl` to be installed and available on the path for Flyway to invoke. `JDBC` is still used to query the database for some Flyway operations, and 
`SQLcl` will be used for executing migrations.

### Site Profiles (`glogin.sql`) & User Profiles (`login.sql`)

This feature allows you to set up your `SQLcl` environment to use the same settings with each session. It allows you to execute statements before every script run, and is typically used to configure
the session in a consistent manner by calling SQL*Plus commands such as `SET FEEDBACK` and `SET DEFINE`.

Flyway will look for `login.sql` in all the valid migration locations, and load it if present. `glogin.sql` will be loaded from `$ORACLE_HOME/sqlplus/admin/glogin.sql` in UNIX, and `ORACLE_HOME\sqlplus\admin\glogin.sql` otherwise.

### Variable substitution

Non-Native Connectors Flyway supports `SQL\*Plus` and `SQLcl` variable substitution (`&VAR`-style) and can populate them with Flyway placeholders automatically.

Native Connectors does not support this feature, and will hang if the native tool cannot find the variable. Ensure all `&VAR` style variables are defined in the executing scripts to avoid this situation.

Flyway [placeholders](<Configuration/Flyway Namespace/Flyway Placeholders Namespace>) are available to be used but not in scripts referenced from within a Flyway script.

## Authentication

### JDBC

Oracle supports user and password being provided in the JDBC URL, in the form

`jdbc:oracle:thin:<user>/<password>@//<host>:<port>/<database>`

In this case, they do not need to be passed separately in configuration.

### Oracle Wallet

{% include teams.html %}

Flyway can connect to your databases using credentials in your Oracle Wallet.

First you need to ensure you have set the environment variable `TNS_ADMIN` to point to the location containing your `tnsnames.ora` file. Then you will need to configure the [`flyway.oracle.walletLocation`](<Configuration/Flyway Namespace/Flyway Oracle Namespace/Flyway Oracle Wallet Location Setting>) parameter to point to the location of your Oracle wallet. Lastly your URL should be provided as specified in `tnsnames.ora` i.e. if it is using an alias then connect with the `jdbc:oracle:thin:@db_alias` syntax.

With that configuration you will be able to connect to your database without providing any credentials in config.

## Limitations

- SPATIAL EXTENSIONS: `sdo_geom_metadata` can only be cleaned for the user currently logged in

### SQL*Plus

#### Referenced scripts and checksums

Native Connectors for Oracle will not use referenced scripts, `login.sql`, or `glogin.sql` when calculating checksums. They will therefore likely differ and [`repair`](<Commands/Repair>) will need to be run.

This is because non-Native Connectors Flyway included any referenced scripts when calculating checksums. This also extended to `login.sql` and `glogin.sql` since their contents can affect the reproducibility of a migration and can differ in different environments.

## Flashback Restore Points (Enterprise Edition)

Flyway Enterprise supports the use of Oracle Flashback Database restore points as a safety mechanism during migrations. When running with the appropriate Oracle privileges and Flashback Database enabled, Flyway will automatically create a restore point at the start of a migration and drop it on commit. If a migration fails, the restore point can be used to manually restore the database to its previous state.

> **Note:** This feature replaces Flyway's traditional transaction management for Oracle Native Connectors. It does **not** use Oracle transactions. Instead, it leverages Oracle's Flashback Database capability to provide a rollback mechanism at the database level.

**Requirements:**
- Oracle Flashback Database must be enabled.
- The connected user must have the `CREATE ANY RESTORE POINT`, `SYSDBA`, or `SYSOPER` privilege.
- Available in Flyway Enterprise Edition only.

**How it works:**
- At the start of a migration, Flyway creates a restore point named `FLYWAY_RP_<timestamp>`.
- On successful commit, the restore point is dropped.
- On rollback, Flyway logs the restore point name for manual use. Actual database flashback must be performed manually by a DBA.

**Limitations:**
- Flashback restore points affect the entire database and require downtime to restore.
- Automatic flashback is not performed by Flyway; only the restore point is created and managed.
