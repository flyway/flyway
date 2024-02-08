---
subtitle: Azure Active Directory Interactive Resolver
---
# Azure Active Directory Interactive Resolver
This is a [property resolver](Configuration/Property Resolvers) which applies when trying to connect to Microsoft Azure SQL Server databases using interactive authentication.
Connections made using this form of authentication without this resolver will have the default behavior of prompting for login on every database call.
When using this resolver, with the appropriate Azure setup, documented [here](https://documentation.red-gate.com/flyway/learn-more-about-flyway/database-connections-in-flyway-desktop/using-azure-interactive-authentication), you can connect once and your token will be cached and reused for a period of time.

To configure this set the value of the `accessToken` [JDBC property](<Configuration/Parameters/Environments/JDBC Properties>) to `${azureAdInteractive.token}` and supply the `tenantId` and `clientId` resolver properties.
Alternatively, this can be configured using Flyway Desktop.

## Example
This can be used in the TOML configuration like this:
```toml
[environments.development]
url = "jdbc:sqlserver://mfa-testing.database.windows.net:1433;databaseName=MyDatabase"

[environments.developemnt.jdbcProperties]
accessToken = "${azureAdInteractive.token}"

[environments.developemnt.resolvers.azureAdInteractive]
tenantId = "{some GUID}"
clientId = "{some other GUID}"
```

