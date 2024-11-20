---
subtitle: Backup Provisioner
---
# Backup Provisioner - Preview
{% include enterprise.html %}

This [provisioner](Configuration/Provisioners) allows for the provisioning and re-provisioning of databases using a database backup file.

**Note: Currently only SQL Server `.bak` files are supported.**

Benefits of using the backup provisioner:
* The database backup can contain static data and a `flyway_schema_history` table, allowing an environment to be provisioned with data to a desired version.
* Restoring a database backup is not impacted by references to invalid objects. This makes the backup provisioner a good alternative to a baseline script.
* Speeding up shadow provisioning - restoring a backup file that represents version 1000 is considerably quicker than running 1000 migrations scripts.

## Prerequisites
* A database backup `.bak` file, which could be a backup of production database for example. This backup file needs to be in a location accessible to the database server that will be provisioned i.e. on the database server itself or on a network share.
* The environment [URL](Configuration/Parameters/Environments/URL) must have the `databaseName` parameter set to the name of the database that the backup will be restored to. If this database doesn't yet exist on the target server then it will be created by the backup provisioner.

## To configure this provisioner:
1. Set the value of the [provisioner parameter](Configuration/Parameters/Environments/Provisioner) to `backup`.
2. Populate the following resolver properties in the TOML configuration or as command line arguments:
    - `backupFilePath` - (Required) The file path of the backup file. Note: this needs to be accessible/relative to the database server that is being provisioned.
    - `backupVersion` - (Optional) The migration version the backup file represents. This property is required when the backup file doesn't contain a `flyway_schema_history` table. In this scenario a `flyway_schema_history` table will be created once the backup has been restored and a baseline entry with version `backupVersion` will be inserted into the `flyway_schema_history` table. If the backup file does contain a `flyway_schema_history` table then this property is optional. If left unset then the `flyway_schema_history` table from the backup will be restored unaltered, otherwise the `flyway_schema_history` will be updated to baseline version `backupVersion`.
  
## Example
The backup provisioner can be used in the TOML configuration as follows:
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
restore the backup using these generated file paths. The example toml below shows how this can be enabled:

```toml
[environments.shadow.resolvers.backup]
backupFilePath = '/tmp/backup/backup.bak'
backupVersion = "995"
sqlserver.generateWithMove = true
```

## Specify data and log file paths

An alternative to the `generateWithMove` parameter above is to specify the exact file path that data and log files
should be restored to. The example toml below shows how this can be done:

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