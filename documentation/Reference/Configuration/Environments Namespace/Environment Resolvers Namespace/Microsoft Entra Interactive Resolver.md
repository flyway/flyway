---
subtitle: Microsoft Entra Interactive Resolver
---
This is a [property resolver](https://documentation.red-gate.com/flyway/flyway-concepts/environments/resolvers) which applies when trying to connect to Microsoft Azure SQL Server databases using interactive authentication.
Connections made using this form of authentication without this resolver will have the default behavior of prompting for login on every database call.
When using this resolver, with the appropriate Azure setup, documented [here](https://documentation.red-gate.com/flyway/learn-more-about-flyway/database-connections-in-flyway-desktop/using-azure-interactive-authentication), you can connect once and your token will be cached and reused for a period of time.

This resolver can be referenced as either `entraId` or `azureAdInteractive`; the two names are interchangeable.

The initial sign-in is interactive and opens a browser for you to authenticate, so this resolver is not recommended for use in CI systems or other non-interactive workflows. Use one of the non-interactive Microsoft Entra methods instead, such as a service principal or a managed identity, described in [Connecting to environments](https://documentation.red-gate.com/flyway/database-development-using-flyway/connecting-to-environments#Connectingtoenvironments-Authentication). Those methods are handled by the database driver and do not use the token cache described below.

{% include anchor.html link="token-cache-requirements"%}
## Token cache requirements

So that it does not prompt for login on every database call, this resolver caches your token using the operating system's secure credential storage, by way of the Microsoft Authentication Library (MSAL). That storage has to be present and working, otherwise the resolver fails when it initializes the cache.

The cache is written to `flyway_msal_tokenCache.dat` in the `Redgate/flyway` folder of your home directory. If a cached token stops working, deleting this file forces a fresh sign-in.

### Windows

The Data Protection API (DPAPI) is used. No additional setup is required.

### Mac

The Mac keychain is used. No additional setup is required.

### Linux

Libsecret is used, and it must be able to reach a working Secret Service implementation. All of the following are required:

- the Libsecret library is installed (for example, the `libsecret-1-0` package on Debian and Ubuntu)
- a Secret Service provider, such as gnome-keyring, is installed and running
- a D-Bus session bus is available for Libsecret to reach that provider over
- the keyring it stores into is unlocked

Installing Libsecret by itself is not enough, because without a running, unlocked keyring on a session bus there is nothing for it to talk to. This is the usual failure on headless machines and CI agents, which by default tend to have neither a D-Bus session nor a keyring daemon. Getting it working there generally means installing a keyring daemon, starting it under a D-Bus session that lasts for the duration of the Flyway invocation, and unlocking it with a known password.

## Settings

| Setting                                                                                                                                                                                        | Required | Type   | Description                           |
|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------|--------|---------------------------------------|
| [`tenantId`](<Configuration/Environments Namespace/Environment Resolvers Namespace/Microsoft Entra Interactive Resolver/Microsoft Entra Interactive Resolver Tenant Id Setting>) | Yes      | String | The Microsoft Entra tenant id. |
| [`clientId`](<Configuration/Environments Namespace/Environment Resolvers Namespace/Microsoft Entra Interactive Resolver/Microsoft Entra Interactive Resolver Client Id Setting>) | Yes      | String | The Microsoft Entra client id. |

## Usage

### Flyway Desktop

This can be set from the connection dialog.

### TOML Configuration File

```toml
[environments.development]
url = "jdbc:sqlserver://mfa-testing.database.windows.net:1433;databaseName=MyDatabase"

[environments.development.jdbcProperties]
accessToken = "${entraId.token}"

[environments.development.resolvers.entraId]
tenantId = "{some GUID}"
clientId = "{some other GUID}"
```

