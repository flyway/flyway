---
subtitle: environments.*.resolvers.gcsm.project
---

{% include enterprise.html %}

## Description

The [Vault](https://www.vaultproject.io/) token required to access your secrets.

## Type

String

## Default

<i>none</i>

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```bash
./flyway info -environments.development.resolvers.vault.token="abc.1234567890"
```

### TOML Configuration File

```toml
[environments.development.resolvers.vault]
token = "abc.1234567890"
```
