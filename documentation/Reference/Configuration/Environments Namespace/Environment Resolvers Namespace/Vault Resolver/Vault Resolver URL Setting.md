---
subtitle: environments.*.resolvers.gcsm.project
---

{% include enterprise.html %}

## Description

The REST API URL of your [Vault](https://www.vaultproject.io/) server, including the API version. Currently only supports API version v1.

Example: `http://localhost:8200/v1/`

## Type

String

## Default

<i>none</i>

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```bash
./flyway info -environments.development.resolvers.vault.url="http://localhost:8200/v1"
```

### TOML Configuration File

```toml
[environments.development.resolvers.vault]
url = "http://localhost:8200/v1"
```
