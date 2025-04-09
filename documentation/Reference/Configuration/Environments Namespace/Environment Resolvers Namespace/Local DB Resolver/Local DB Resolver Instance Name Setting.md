---
subtitle: environments.*.resolvers.gcsm.project
---

{% include enterprise.html %}

## Description

The instance name of your SQL Server local DB.
This is most commonly `MSSQLLocalDB`.

## Type

String

## Default

<i>none</i>

## Usage

### Flyway Desktop

This can be set from the connection dialog for SQL Server project by selecting the jTds driver.

### Command-line

```bash
./flyway info -environments.development.resolvers.localdb.instanceName='MSSQLLocalDB'
```

### TOML Configuration File

```toml
[environments.development.resolvers.localdb]
instanceName = "MSSQLLocalDB"
```
