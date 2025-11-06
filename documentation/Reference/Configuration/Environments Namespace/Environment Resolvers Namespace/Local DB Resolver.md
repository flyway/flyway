---
subtitle: Local DB Resolver
---

This is a very specific [property resolver](https://documentation.red-gate.com/flyway/flyway-concepts/environments/resolvers) which only applies when trying to connect to SQL Server local DB.
Local DB is only supported by the JTDS JDBC driver and can only be connected to using named pipes - you can read more about this limitation in [this GitHub issue](https://github.com/microsoft/mssql-jdbc/issues/769#issuecomment-420020261).
This means that the instance name for your LocalDB will change every time the service is restarted, which would mean you'd have to edit your connection string every time too.
This resolver allows you to avoid this by automatically querying for the instance name before each connection.

This resolver requires the SqlLocalDB utility to be available locally in order to function.

## Settings

| Setting                                                                                                                                            | Required | Type   | Description                                    |
|----------------------------------------------------------------------------------------------------------------------------------------------------|----------|--------|------------------------------------------------|
| [`instanceName`](<Configuration/Environments Namespace/Environment Resolvers Namespace/Local DB Resolver/Local DB Resolver Instance Name Setting>) | Yes      | String | The instance name of your SQL Server local DB. |

## Usage

### Flyway Desktop

This can be set from the connection dialog for SQL Server project by selecting the jTds driver.

### Command-line

```bash
./flyway info \
-environment='development' \
-environments.development.url='jdbc:jtds:sqlserver://./development;instance=${localdb.pipeName};namedPipe=true' \
-environments.development.user='my-user' \
-environments.development.password='${localSecret.developmentPassword}' \
-environments.development.resolvers.localdb.instanceName='MSSQLLocalDB'
```

### TOML Configuration File

```toml
[environments.development]
url = "jdbc:jtds:sqlserver://./development;instance=${localdb.pipeName};namedPipe=true"
user = "my-user"
password = "${localSecret.developmentPassword}"

[environments.development.resolvers.localdb]
instanceName = "MSSQLLocalDB"
```
