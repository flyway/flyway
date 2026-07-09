---
subtitle: environments.*.resolvers.azureKeyVault.vaultUrl
---

{% include enterprise.html %}

## Description

The URL of your [Azure Key Vault](https://azure.microsoft.com/en-us/products/key-vault).

Example: `https://my-vault.vault.azure.net`

## Type

String

## Default

<i>none</i>

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honored, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```bash
./flyway info -environments.development.resolvers.azureKeyVault.vaultUrl="https://my-vault.vault.azure.net"
```

### TOML Configuration File

```toml
[environments.development.resolvers.azureKeyVault]
vaultUrl = "https://my-vault.vault.azure.net"
```
