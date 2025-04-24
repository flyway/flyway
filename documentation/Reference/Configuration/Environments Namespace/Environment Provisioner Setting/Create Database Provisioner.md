---
subtitle: Create Database Provisioner
---

- **Status:** Preview

{% include enterprise.html %}

This [provisioner](https://documentation.red-gate.com/flyway/flyway-concepts/environments/provisioning) provisions and
re-provisions a databases by creating it if necessary.

Benefits of using the create-database provisioner:

* The database doesn't have to exist on the target database server before using it with Flyway verbs, as the database
  will be created when required.
* When re-provisioned, the database will be reset back to a clean state if it already exists.

## Supported database engines

The following database engines are currently supported:

* SQL Server
* MySQL
* PostgreSQL

## To configure this provisioner:

1. Set the value of the [provisioner parameter](<Configuration/Environments Namespace/Environment Provisioner Setting>)
   to `create-database`.
2. Ensure that the database name is present in the JDBC URL. Each database engine has a different way of specifying the
   database name, please see the example configuration below.

**Note**: The user account specified for the environment will need the required permissions to create a database,
otherwise Flyway will exit with an error when it attempts to create the database.

Below we consider configuration and examples for each supported database engine types.

## SQL Server Example Configuration

```toml
[environments.shadow]
url = "jdbc:sqlserver://localhost:1433;databaseName=MyDatabase;trustServerCertificate=true"
user = "MyUser"
password = "${localSecret.MyPasswordKey}"
provisioner = "create-database"
```

This example will create the database, named `MyDatabase`, if it does not exist when Flyway connects to the database
server. The create-database provisioner uses the `databaseName` parameter from the JDBC URL to determine the database to
create.

## PostgreSQL Example Configuration

```toml
[environments.shadow]
url = "jdbc:postgresql://localhost:5430/my_database?ssl=true"
user = "MyUser"
password = "${localSecret.MyPasswordKey}"
provisioner = "create-database"
```

This example will create the database, named `my_database`, if it does not exist when Flyway connects to the database
server. The create-database provisioner extracts the database name from the JDBC URL to determine the database to
create.

## MySQL Example Configuration

```toml
[environments.shadow]
url = "jdbc:mysql://localhost:3306/my_database?useSSL=false"
user = "MyUser"
password = "${localSecret.MyPasswordKey}"
provisioner = "create-database"
```

This example will create the database, named `my_database`, if it does not exist when Flyway connects to the database
server. The create-database provisioner extracts the database name from the JDBC URL to determine the database to
create.

## `allowClean` Option

The `create-database` provisioner supports an `allowClean` boolean option which defaults to `true`. This parameter
controls whether the specified database should be cleaned when it already exists on reprovision.

For example, if the create-database provisioner is configured with a URL containing the database name `TestDatabase`,
and that database already exists, then when the environment is re-provisioned the `TestDatabase` database will be
cleaned.

The example configuration below shows how to disable this option for the shadow environment:

```toml
[environments.shadow.resolvers.create-database]
allowClean = false
```