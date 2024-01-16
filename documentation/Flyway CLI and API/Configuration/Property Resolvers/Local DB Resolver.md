---
subtitle: Local DB Resolver
---
# Local DB Resolver
This is a very specific [property resolver](Configuration/Property Resolvers) which only applies when trying to connect to SQL Server local DB.
Local DB is only supported by the JTDS JDBC driver and can only be connected to using named pipes - you can read more about this limitation in [this GitHub issue](https://github.com/microsoft/mssql-jdbc/issues/769#issuecomment-420020261).
This means that the instance name for your LocalDB will change every time the service is restarted, which would mean you'd have to edit your connection string every time too.
This resolver allows you to avoid this by automatically querying for the instance name before each connection.

To configure this simply set the value of the instance name in your JDBC URL to `${localdb.pipeName}` and configure the `instanceName` resolver property.

## Example
This can be used in the TOML configuration like this:
```toml
[environments.development]
url = "jdbc:jtds:sqlserver://./development;instance=${localdb.pipeName};namedPipe=true"
user = "my-user"
password = "${localSecret.developmentPassword}"

[environments.development.resolvers.localdb]
instanceName = "MSSQLLocalDB"
```

