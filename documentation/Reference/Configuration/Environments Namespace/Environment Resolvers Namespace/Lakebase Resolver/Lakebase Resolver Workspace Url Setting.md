---
subtitle: environments.*.resolvers.lakebase.workspaceUrl
---

## Description

The URL of the Databricks workspace hosting the Lakebase instance.

## Type

String

## Default

<i>none</i>

## Usage

### Command-line

```bash
./flyway info -environments.development.resolvers.lakebase.workspaceUrl='https://adb-1234567890123456.7.azuredatabricks.net'
```

### TOML Configuration File

```toml
[environments.development.resolvers.lakebase]
workspaceUrl = "https://adb-1234567890123456.7.azuredatabricks.net"
```
