---
subtitle: Oracle - Native Connectors
---

- **Status:** {% include preview.html %}
- **Verified Versions:** 11.1, 21
- **Maintainer:** {% include redgate-badge.html %}
- **Edition:** Redgate

All Oracle editions are supported, including XE.

## Supported Versions and Support Levels

{% include database-boilerplate.html %}

## Driver

| Item                               | Details                                                                      |
|------------------------------------|------------------------------------------------------------------------------|
| **URL format**                     | `jdbc:oracle:thin:@//host:port/service` <br> `jdbc:oracle:thin:@tns_entry` * |
| **Ships with Flyway Command-line** | Yes                                                                          |
| **Maven Central coordinates**      | `com.oracle.database.jdbc:ojdbc11`                                           |
| **Supported versions**             | Oracle Database versions - 21c, 19c, 18c, and 12.2                           |
| **Default Java class**             | `oracle.jdbc.OracleDriver`                                                   |

\* `TNS_ADMIN` environment variable must point to the directory of where `tnsnames.ora` resides

## Using Flyway with Oracle Native Connectors

Flyway's Native Connector support requires:
- Setting environment variable "FLYWAY_NATIVE_CONNECTORS=true"
- Oracle's `SQLcl` to be installed and a path environment variable pointing to the `SQLcl` bin directory 

`JDBC` is still used to query the database for some Flyway operations, and `SQLcl` will be used for executing migrations.

### Site Profiles (`glogin.sql`) & User Profiles (`login.sql`)

This feature allows you to set up your `SQLcl` environment to use the same settings with each session. It allows you to execute statements before every script run, and is typically used to configure
the session in a consistent manner by calling SQL*Plus commands such as `SET FEEDBACK` and `SET DEFINE`.

Flyway will look for `login.sql` in all the valid migration locations, and load it if present. `glogin.sql` will be loaded from `$ORACLE_HOME/sqlplus/admin/glogin.sql` in UNIX, and `ORACLE_HOME\sqlplus\admin\glogin.sql` otherwise.

### Variable substitution

Non-Native Connectors Flyway supports `SQL\*Plus` and `SQLcl` variable substitution (`&VAR`-style) and can populate them with Flyway placeholders automatically.

Native Connectors does not support this feature, and will hang if the native tool cannot find the variable. Ensure all `&VAR` style variables are defined in the executing scripts to avoid this situation.

Flyway [placeholders](<Configuration/Flyway Namespace/Flyway Placeholders Namespace>) are available to be used but not in scripts referenced from within a Flyway script.

## Authentication

Oracle supports several methods of authentication, including username/password (which can be embedded in the JDBC URL), Oracle Wallet, and Kerberos.

For how to use authentication methods, see [Connecting to environments](https://documentation.red-gate.com/flyway/database-development-using-flyway/connecting-to-environments#Connectingtoenvironments-Authentication).
For credential storage and retrieval, see [Storing and retrieving credentials](https://documentation.red-gate.com/flyway/database-development-using-flyway/storing-and-retrieving-credentials#Storingandretrievingcredentials-Database-specificcredentialretrieval).

Oracle also supports user and password being provided directly in the JDBC URL:

`jdbc:oracle:thin:<user>/<password>@//<host>:<port>/<database>`

In this case, they do not need to be passed separately in configuration.

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
