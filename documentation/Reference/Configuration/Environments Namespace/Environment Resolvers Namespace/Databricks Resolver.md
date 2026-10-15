---
subtitle: Databricks Resolver
---
This is a [property resolver](https://documentation.red-gate.com/flyway/flyway-concepts/environments/resolvers) which applies when connecting to Databricks using browser-based OAuth (`AuthMech=11;Auth_Flow=2`).

The Databricks JDBC driver encrypts its cached OAuth tokens with the passphrase given in the `TokenCachePassPhrase` JDBC property, so that later connections can reuse a sign-in. The driver caches tokens by default and won't connect with browser-based OAuth unless a passphrase is set. `${databricks.tokenCachePassPhrase}` supplies a passphrase that is unique to each user, so it doesn't need to be written in the configuration.

The first time it is resolved, Flyway generates a random passphrase and saves it in the user's secret store as `Flyway Databricks token cache`, in the same way as the [Local Secret resolver](<Configuration/Environments Namespace/Environment Resolvers Namespace/Local Secret Resolver>). Later runs read it back from there.

## Usage

```toml
[environments.development]
url = "jdbc:databricks://adb-1234567890123456.7.azuredatabricks.net:443/default;httpPath=/sql/1.0/warehouses/abc123;AuthMech=11;Auth_Flow=2"

[environments.development.jdbcProperties]
TokenCachePassPhrase = "${databricks.tokenCachePassPhrase}"
```

The resolver needs no settings.
