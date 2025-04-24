---
subtitle: Backup Provisioner
---

- **Status:** Preview

{% include enterprise.html %}

This [provisioner](https://documentation.red-gate.com/flyway/flyway-concepts/environments/provisioning) allows for the provisioning and re-provisioning of databases using a database backup file.

Benefits of using the backup provisioner:
* The database backup can contain static data and a `flyway_schema_history` table, allowing an environment to be provisioned with data to a desired version.
* Restoring a database backup is not impacted by references to invalid objects. This makes the backup provisioner a good alternative to a baseline script.
* Speeding up shadow provisioning - restoring a backup file that represents version 1000 is considerably quicker than running 1000 migrations scripts.

## Supported backup file formats

The following database engines and backup file formats are supported:

* SQL Server backup files (`.bak`).
* Oracle dump files (`.dmp`) generated using the Data Pump Export tool `expdp`.

## Prerequisites

* A database backup file, which could be a backup of production database for example. This backup file needs to be in a
  location accessible to the database server that will be provisioned i.e. on the database server itself or on a network
  share.

**For SQL Server**:

* The environment [URL](<Configuration/Environments Namespace/Environment URL Setting>) must have the `databaseName`
  parameter set to the name of the database that the backup will be restored to. If this database doesn't yet exist on
  the target server then it will be created by the backup provisioner.

**For Oracle**:

* The schemas that will be restored from the dump file must already exist on the target database.
* The Oracle Data Pump Import tool `impdp` must be installed and available on the `PATH` of the machine running Flyway.
* The user specified for the environment must have the `IMP_FULL_DATABASE` privilege, and a `READ` privilege on the
  directory where the dump file is located.

## To configure this provisioner:
1. Set the value of the [provisioner parameter](<Configuration/Environments Namespace/Environment Provisioner Setting>) to `backup`.
2. Populate the following resolver properties in the TOML configuration or as command line arguments:
    - `backupFilePath` - (Required) The file path of the backup file. Note: this needs to be accessible/relative to the database server that is being provisioned.
    - `backupVersion` - (Optional) The migration version the backup file represents. This property is required when the backup file doesn't contain a `flyway_schema_history` table. In this scenario a `flyway_schema_history` table will be created once the backup has been restored and a baseline entry with version `backupVersion` will be inserted into the `flyway_schema_history` table. If the backup file does contain a `flyway_schema_history` table then this property is optional. If left unset then the `flyway_schema_history` table from the backup will be restored unaltered, otherwise the `flyway_schema_history` will be updated to baseline version `backupVersion`.

Below we consider configuration and examples for each supported database engine.

# SQL Server

## Example Configuration
The backup provisioner can be configured in the TOML file as follows:
```toml
[environments.shadow]
url = "jdbc:sqlserver://localhost:1433;databaseName=MyDatabase;trustServerCertificate=true"
user = "MyUser"
password = "${localSecret.MyPasswordKey}"
provisioner = "backup"

[environments.shadow.resolvers.backup]
backupFilePath = '\\DBOps1\Backups\backup.bak'
backupVersion = "995"
```

This example will restore the backup file located at `\\DBOps1\Backups\backup.bak` to the `MyDatabase` database in the shadow environment. The `MyDatabase` database will be at version 995. This example represents a common scenario where a user may wish to reset the shadow environment to the same state as production, so that new development migrations can then be applied to the shadow and verified.

## SQL Server WITH MOVE

The T-SQL "WITH MOVE" syntax makes it possible to specify the file paths on disk that data and log files should be
restored to. This can be required in a couple of scenarios:

- The directory structure on the target database doesn't match that of the source database. For example, the source
  database stores data and log files under the `C:` drive, whilst the target database has no `C:` drive and stores
  database files under the `D:` drive.
- The data and log file paths embedded in the backup file are already being used by a different database. For example,
  taking a database backup and restoring it on the same SQL Server instance but under a different database name, so
  that the restored database exists alongside the original. In this scenario, new file paths to restore the data and log
  files must be provided when performing the restore.

The backup provisioner provides two methods for adjusting the data and log files paths when restoring a database.

## Auto-generate data and log file paths

The backup provisioner exposes a `generateWithMove` boolean parameter, which defaults to false. When set to true, the
backup provisioner will auto-generate file paths for any data and log files contained within the backup file, and
restore the backup using these generated file paths. The example TOML below shows how this can be enabled:

```toml
[environments.shadow.resolvers.backup]
backupFilePath = '/tmp/backup/backup.bak'
backupVersion = "995"
sqlserver.generateWithMove = true
```

## Specify data and log file paths

An alternative to the `generateWithMove` parameter above is to specify the exact file path that data and log files
should be restored to. The example TOML below shows how this can be done:

```toml
[environments.shadow.resolvers.backup]
backupFilePath = '/tmp/backup/backup.bak'
backupVersion = "995"

[[environments.shadow.resolvers.backup.sqlserver.files]]
logicalName = "NewWorldDB"
filePath = "/var/opt/mssql/data/NewWorldDB_shadow_data.mdf"

[[environments.shadow.resolvers.backup.sqlserver.files]]
logicalName = "NewWorldDB_log"
filePath = "/var/opt/mssql/data/NewWorldDB_shadow_log.ldf"
```

Where:

- `logicalName` is the logical name of a data or log file in the backup file.
- `filePath` is the file path on disk where the logical file will be restored to.

**Note**: When file paths are provided as above, then the `generateWithMove` parameter is ignored.
i.e. User defined file paths take precedence over auto-generated file paths.

# Oracle

## Additional parameters

The backup provisioner supports the following additional parameters for Oracle databases:

* `connectionIdentifier` (Required) - The connect identifier used to connect to the target database i.e. An Oracle*Net
  connect descriptor or a net service name (usually defined in the `tnsnames.ora` file). The `connectionIdentifier` must
  contain or map to all data required to log in to the database without any user interaction. That is, using the
  `connectionIdentifier` should not require a username or password to be entered.
* `importContent` (Optional) - The content to restore to the target schema. Valid values are:
    * `METADATA_ONLY` (Default) - Loads only database object definitions i.e. no data.
    * `ALL` - Loads database object definitions and data.
* `parFilePath` (Optional) - The file path to a Data Pump Import parameter file. This option can be used to customize
  the parameters that Flyway passes into `impdp`, and provide additional parameters. For example, the `PARALLEL`
  parameter can specified to speed up the import time by increasing the number of active execution processes operating
  on behalf of the import job.
  Note, that the following parameters cannot be set in the parameter file: `CONTENT`, `DUMPFILE`, `SCHEMAS`,
  `REMAP_SCHEMA`, as they're already set by Flyway. More information on the Data Pump Import utility and the parameters
  it supports can be
  found [here](https://docs.oracle.com/en/database/oracle/oracle-database/19/sutil/datapump-import-utility.html#).
* `ignoreErrors` (Optional) - A boolean parameter which dictates whether flyway will error if `impdp` exits with a
  non-zero code. Valid values are:
    * `false` (Default) - Flyway errors and exits early if the `impdp` command exits with a non-zero error code.
    * `true` - Flyway ignores the exit code of `impdp`. This option can be useful when non-critical objects fail to
      import and can safely be ignored. However, it should ideally only be used when necessary, due to the database
      being left in a partially restored state.
* `schemaMapping` (Optional) - A map specifying the new schema names for the schemas in the dump file. The key is the
  schema name in the dump file and the value is the new schema name that it should be mapped to. This is useful when we
  wish to restore a schema from the dump file to a different schema name on the target database.

**Note**, the parameters above should be prefixed with `oracle.` in the TOML configuration. Please see the example
configuration below.

## Example Configuration

The backup provisioner can be configured in the TOML configuration as follows:

```toml
[environments.shadow]
url = "jdbc:oracle:thin:@//localhost:1521/XE"
user = "DEV"
password = "${localSecret.MyPasswordKey}"
schemas = ["SHADOW"]
provisioner = "backup"

[environments.shadow.resolvers.backup]
backupFilePath = "DATA_PUMP_DIR:dev.dmp"
backupVersion = "995"
oracle.connectionIdentifier = "/@MYALIAS"
oracle.importContent = "METADATA_ONLY"
oracle.ignoreErrors = false

[environments.shadow.resolvers.backup.oracle.schemaMapping]
"DEV" = "SHADOW"
```

This example will restore the backup file located at `DATA_PUMP_DIR:dev.dmp` to the `SHADOW` schema in the shadow
environment. The `SHADOW` schema will be at version 995.

Some additional points to note are:

* A backup of the `DEV` schema has been taken and exists at the location `DATA_PUMP_DIR:dev.dmp`. The dump file location
  is given in the format `DIRECTORY:filename` where `DATA_PUMP_DIR` is a default Data Pump directory available to use,
  and `dev.dmp` is the dump file name.
* The `DEV` schema in the dump file will be restored to the `SHADOW` schema in the target shadow environment.
* `MYALIAS` is a `tnsnames.ora` connection alias has been defined in the `tnsnames.ora` and an Oracle wallet with a
  password for `MYALIAS` has been created and configured in the `sqlnet.ora`.
* Flyway can be run in debug mode, using the `-X` parameter, to see the output of the `impdp` command.

If you do not wish to set up a `tnsnames.ora` connection alias or Oracle wallet, then a connection string of the form
`username/password@[//]host[:port][/service_name]` can be used instead for the `connectionIdentifier`. For example, a
`DEV` user with the password `DEV_PASSWORD` connecting to a database on `localhost` with the service name `XE` on port
`1521` could have the `connectionIdentifier` specified as follows:

```toml
oracle.connectionIdentifier = "DEV/DEV_PASSWORD@localhost:1521/XE"
```

The `connectionIdentifier` parameter also supports property resolution, so the connection string can instead be
specified using a property resolver, to avoid hard coding details like the password in the TOML file. The example below
uses the [Local Secret](<Configuration/Environments Namespace/Environment Resolvers Namespace/Local DB Resolver>)
resolver to encode the `connectionIdentifier`:

```toml
oracle.connectionIdentifier = "${localSecret.DevConnectIdentifier}"
```