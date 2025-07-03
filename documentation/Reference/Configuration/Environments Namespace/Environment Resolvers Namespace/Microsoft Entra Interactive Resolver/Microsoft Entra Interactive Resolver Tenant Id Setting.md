---
subtitle: environments.*.resolvers.gcsm.project
---

{% include enterprise.html %}

## Description

The Microsoft Entra tenant id.

## Type

String

## Default

<i>none</i>

## Usage

### Flyway Desktop

This can be set from the connection dialog for SQL Server projects.

### Command-line

```bash
./flyway info -environments.development.resolvers.azureAdInteractive.tenantId='{some GUID}'
```

### TOML Configuration File

```toml
[environments.development.resolvers.azureAdInteractive]
tenantId = "{some GUID}"
```
