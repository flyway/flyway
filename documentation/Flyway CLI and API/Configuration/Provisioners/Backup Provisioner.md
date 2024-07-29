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
* The environment [URL](Configuration/Parameters/Environments/URL) must have the `databaseName` parameter set to the name of the database that the backup will be restored to.
* The database `databaseName` must already exist on the database server that will be provisioned, but can be empty.

## To configure this provisioner:
1. Set the value of the [provisioner parameter](Configuration/Parameters/Environments/Provisioner) to `backup`.
2. Populate the following resolver properties in the TOML configuration on as command line arguments:
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
