---
subtitle: Azure Active Directory Interactive Resolver
---

This is a [property resolver](https://documentation.red-gate.com/flyway/flyway-concepts/environments/resolvers) which applies when trying to connect to Microsoft Azure SQL Server databases using interactive authentication.
Connections made using this form of authentication without this resolver will have the default behavior of prompting for login on every database call.
When using this resolver, with the appropriate Azure setup, documented [here](https://documentation.red-gate.com/flyway/learn-more-about-flyway/database-connections-in-flyway-desktop/using-azure-interactive-authentication), you can connect once and your token will be cached and reused for a period of time.

## Settings

| Setting                                                                                                                                                                                        | Required | Type   | Description                           |
|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------|--------|---------------------------------------|
| [`tenantId`](<Configuration/Environments Namespace/Environment Resolvers Namespace/Azure Active Directory Interactive Resolver/Azure Active Directory Interactive Resolver Tenant Id Setting>) | Yes      | String | The Azure Active Directory tenant id. |
| [`clientId`](<Configuration/Environments Namespace/Environment Resolvers Namespace/Azure Active Directory Interactive Resolver/Azure Active Directory Interactive Resolver Client Id Setting>) | Yes      | String | The Azure Active Directory client id. |

## Usage

### Flyway Desktop

This can be set from the connection dialog.

### TOML Configuration File

```toml
[environments.development]
url = "jdbc:sqlserver://mfa-testing.database.windows.net:1433;databaseName=MyDatabase"

[environments.development.jdbcProperties]
accessToken = "${azureAdInteractive.token}"

[environments.development.resolvers.azureAdInteractive]
tenantId = "{some GUID}"
clientId = "{some other GUID}"
```

