---
subtitle: environments.*.resolvers.azureKeyVault
---

{% include enterprise.html %}

Per-environment Azure Key Vault secret management configuration.
Values can be inlined in the environment configuration using `${azureKeyVault.key}`.

## Settings

| Setting           | Required | Type   | Description                          |
|-------------------|----------|--------|--------------------------------------|
| [`vaultUrl`](<Configuration/Environments Namespace/Environment Resolvers Namespace/Azure Key Vault Resolver/Azure Key Vault Resolver Vault URL Setting>) | Yes      | String | The URL of the Azure Key Vault.      |

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honored.

### Command-line

```bash
./flyway info \
-environment='development' \
-environments.development.url='jdbc:postgresql://localhost/postgres' \
-environments.development.user='postgres' \
-environments.development.password='${azureKeyVault.my-db-password}' \
-environments.development.resolvers.azureKeyVault.vaultUrl='https://my-vault.vault.azure.net'
```

### TOML Configuration File

```toml
[environments.development]
url = "jdbc:postgresql://localhost/postgres"
user = "postgres"
password = "${azureKeyVault.my-db-password}"

[environments.development.resolvers.azureKeyVault]
vaultUrl = "https://my-vault.vault.azure.net"
```
