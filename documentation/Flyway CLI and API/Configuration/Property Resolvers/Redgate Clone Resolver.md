---
subtitle: Redgate Clone Resolver
---
# Redgate Clone Resolver - Preview
{% include enterprise.html %}

This [property resolver](Configuration/Property Resolvers) allows for connecting to databases generated using [Redgate Clone](https://www.red-gate.com/products/redgate-clone/?_ga=2.146246964.1042910986.1704804078-728513631.1704372124).
This is usually used as a provisioner, as detailed [here](Configuration/Provisioners/Redgate Clone Provisioner).

Prerequisites:
* A Redgate Clone server needs to be set up
* The image used for generating the clone needs to already exist on the clone server
* (When not setting the `provisioner` setting) The data container to connect to needs to have already been created

To configure this provisioner:
1. Set the value of the [environment JDBC URL parameter](Configuration/Parameters/Environments/URL) to `${clone.url}`. This will derive the JDBC URL from Redgate Clone.
    * Note that currently for SQL Server the database name needs to be set explicitly after this within the URL parameter as shown in the example below (the JDBC URL returned by Redgate Clone always ends with a `;` so additional properties can be appended)
2. Populate the following resolver properties:
    - `url` - (Required)  The Redgate Clone server URL
    - `dataImage` - (Required) The data image to use for creating the container
    - `dataContainer` - (Required) The data container to use for the database clone
    - `dataContainerLifetime` - (Required) The lifetime of the data container. This takes the form of a number optionally followed by a time unit, `s`, `m`, `h`, or `d`. If no time unit is specified, seconds are assumed. `0` can be used to set the lifetime to be unlimited.
    - `authenticationToken` - (Required) The token required for authenticating with the Redgate Clone Server. It is recommended to store this as a secret and resolve it using an appropriate [property resolver](Configuration/Property Resolvers).
    - `operationTimeout` - (Optional) The amount of time to wait for Redgate Clone operations to complete. This takes the form of a number optionally followed by a time unit, `s`, `m`, `h`, or `d`. If no time unit is specified, seconds are assumed. The default value is `5m`.

## Example
This can be used in the TOML configuration like this:
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
