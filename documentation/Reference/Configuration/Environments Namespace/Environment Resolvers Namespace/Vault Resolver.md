---
subtitle: environments.*.resolvers.vault
---

{% include enterprise.html %}

Per-environment Vault secret management configuration.
Values can be inlined in the environment configuration using `${vault.path/to/secret/key}`.

## Settings

| Setting                                                                                                                                        | Required | Type   | Description                           |
|------------------------------------------------------------------------------------------------------------------------------------------------|----------|--------|---------------------------------------|
| [`url`](<Configuration/Environments Namespace/Environment Resolvers Namespace/Vault Resolver/Vault Resolver URL Setting>)                      | Yes      | String | The URL of the Vault API endpoint.    |
| [`token`](<Configuration/Environments Namespace/Environment Resolvers Namespace/Vault Resolver/Vault Resolver Token Setting>)                  | Yes      | String | The token to authenticate with Vault. |
| [`engineName`](<Configuration/Environments Namespace/Environment Resolvers Namespace/Vault Resolver/Vault Resolver Engine Name Setting>)       | Yes      | String | The name of the secret engine.        |
| [`engineVersion`](<Configuration/Environments Namespace/Environment Resolvers Namespace/Vault Resolver/Vault Resolver Engine Version Setting>) | Yes      | String | The version of the secret engine.     |

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured.

### Command-line

```bash
./flyway info \
-environment='development' \
-environments.development.url="jdbc:postgresql://localhost/postgres" \
-environments.development.user="postgres" \
-environments.development.password="${vault.flyway/password}" \
-environments.development.resolvers.vault.url="http://localhost:8200/v1" \
-environments.development.resolvers.vault.token="abc.1234567890" \
-environments.development.resolvers.vault.engineName="secret" \
-environments.development.resolvers.vault.engineVersion="v2"
```

### TOML Configuration File

```toml
[environments.development]
url = "jdbc:postgresql://localhost/postgres"
user = "postgres"
password = "${vault.flyway/password}"

[environments.development.resolvers.vault]
url = "http://localhost:8200/v1"
token = "abc.1234567890"
engineName = "secret"
engineVersion = "v2"
```