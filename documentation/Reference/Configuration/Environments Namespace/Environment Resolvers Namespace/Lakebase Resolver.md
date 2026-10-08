---
subtitle: Lakebase Resolver
---
This is a [property resolver](https://documentation.red-gate.com/flyway/flyway-concepts/environments/resolvers) which applies when connecting to Databricks Lakebase databases using OAuth.

It signs the user in interactively and obtains a fresh database credential each time Flyway connects, so no secret is stored in the Flyway configuration.

## Settings

| Setting                                                                                                                                               | Required | Type   | Description                                                       |
|-------------------------------------------------------------------------------------------------------------------------------------------------------|----------|--------|-------------------------------------------------------------------|
| [`workspaceUrl`](<Configuration/Environments Namespace/Environment Resolvers Namespace/Lakebase Resolver/Lakebase Resolver Workspace Url Setting>) | Yes      | String | The URL of the Databricks workspace hosting the Lakebase project. |
| [`project`](<Configuration/Environments Namespace/Environment Resolvers Namespace/Lakebase Resolver/Lakebase Resolver Project Setting>)            | Yes      | String | The ID of the Lakebase project.                                   |
| [`branch`](<Configuration/Environments Namespace/Environment Resolvers Namespace/Lakebase Resolver/Lakebase Resolver Branch Setting>)              | No       | String | The branch owning the compute endpoint.                           |
| [`compute`](<Configuration/Environments Namespace/Environment Resolvers Namespace/Lakebase Resolver/Lakebase Resolver Compute Setting>)            | No       | String | The compute whose endpoint the credential is issued against.      |
| [`redirectUri`](<Configuration/Environments Namespace/Environment Resolvers Namespace/Lakebase Resolver/Lakebase Resolver Redirect Uri Setting>)   | No       | String | The redirect the browser returns to after signing in.             |

## Usage

```toml
[environments.development]
url = "jdbc:postgresql://ep-my-endpoint.database.uksouth.azuredatabricks.net/databricks_postgres?sslmode=require"
user = "${lakebase.user}"
password = "${lakebase.token}"

[environments.development.resolvers.lakebase]
workspaceUrl = "https://adb-1234567890123456.7.azuredatabricks.net"
project = "my-project"
```

The resolver supplies two values. `${lakebase.token}` is a database credential to use as the password, and `${lakebase.user}` is the Postgres role Lakebase creates for the signed-in Databricks identity, which is that user's email address. A literal address can be used instead.

Flyway opens a browser for the sign-in, and the Databricks SDK caches the resulting token so later runs do not prompt again until the cached token can no longer be refreshed.

`branch` and `compute` default to `production` and `primary`. Set them when connecting through another branch or compute.

Because this flow needs a browser, it is not suitable for automation.
