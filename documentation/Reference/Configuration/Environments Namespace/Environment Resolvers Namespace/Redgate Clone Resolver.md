---
subtitle: Redgate Clone Resolver
---

- **Status:** Preview

{% include enterprise.html %}

This [property resolver](https://documentation.red-gate.com/flyway/flyway-concepts/environments/resolvers) allows for connecting to databases generated using [Redgate Clone](https://www.red-gate.com/products/redgate-clone/?_ga=2.146246964.1042910986.1704804078-728513631.1704372124).
This is usually used as a provisioner, as detailed [here](Configuration/Environments Namespace/Environment Provisioner Setting/Redgate Clone Provisioner).

Prerequisites:

* A Redgate Clone server needs to be set up
* The image used for generating the clone needs to already exist on the clone server
* (When not setting the `provisioner` setting) The data container to connect to needs to have already been created

To use this, configure the relevant settings and set the value of the [environment JDBC URL parameter](<Configuration/Environments Namespace/Environment URL Setting>) to `${clone.url}`. This will derive the JDBC URL from Redgate Clone.
Note that currently for SQL Server the database name needs to be set explicitly after this within the URL parameter as shown in the example below (the JDBC URL returned by Redgate Clone always ends with a `;` so additional properties can be appended)

## Settings

| Setting                                                                                                                                                                         | Required | Type   | Description                                                          |
|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------|--------|----------------------------------------------------------------------|
| [`url`](<Configuration/Environments Namespace/Environment Resolvers Namespace/Redgate Clone Resolver/Redgate Clone Resolver URL Setting>)                                       | Yes      | String | The Redgate Clone server URL.                                        |
| [`dataImage`](<Configuration/Environments Namespace/Environment Resolvers Namespace/Redgate Clone Resolver/Redgate Clone Resolver Data Image Setting>)                          | Yes      | String | The data image to use for creating the container.                    |
| [`dataContainer`](<Configuration/Environments Namespace/Environment Resolvers Namespace/Redgate Clone Resolver/Redgate Clone Resolver Data Container Setting>)                  | Yes      | String | The data container to use for the database clone.                    |
| [`dataContainerLifetime`](<Configuration/Environments Namespace/Environment Resolvers Namespace/Redgate Clone Resolver/Redgate Clone Resolver Data Container Lifetime Setting>) | Yes      | String | The lifetime of the data container.                                  |
| [`authenticationToken`](<Configuration/Environments Namespace/Environment Resolvers Namespace/Redgate Clone Resolver/Redgate Clone Resolver Authentication Token Setting>)      | Yes      | String | The token required for authenticating with the Redgate Clone Server. |
| [`operationTimeout`](<Configuration/Environments Namespace/Environment Resolvers Namespace/Redgate Clone Resolver/Redgate Clone Resolver Operation Timeout Setting>)            | No       | String | The amount of time to wait for Redgate Clone operations to complete. |
 
## Usage

### Flyway Desktop

This can be set from the connection dialog for databases types supported by Redgate Clone.

### Command-line

```bash
./flyway info \
-environment='development' \
-environments.development.url='${clone.url}databaseName=my-database' \
-environments.development.resolvers.clone.url='https://clone.red-gate.com:1234/cloning-api' \
-environments.development.resolvers.clone.dataImage='mssql-empty' \
-environments.development.resolvers.clone.dataContainer='MyContainer' \
-environments.development.resolvers.clone.dataContainerLifetime='1h' \
-environments.development.resolvers.clone.authenticationToken='${localSecret.RedgateCloneToken}'
```

### TOML Configuration File

```toml
[environments.development]
url = "${clone.url}databaseName=my-database"

[environments.development.resolvers.clone]
url = "https://clone.red-gate.com:1234/cloning-api"
dataImage = "mssql-empty"
dataContainer = "MyContainer"
dataContainerLifetime = "1h"
authenticationToken = "${localSecret.RedgateCloneToken}"
```
