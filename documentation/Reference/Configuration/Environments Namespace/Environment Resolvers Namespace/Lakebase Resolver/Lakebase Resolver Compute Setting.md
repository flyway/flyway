---
subtitle: environments.*.resolvers.lakebase.compute
---

## Description

The ID of the Lakebase compute to request a database credential for, within the configured
[`project`](<Configuration/Environments Namespace/Environment Resolvers Namespace/Lakebase Resolver/Lakebase Resolver Project Setting>)
and
[`branch`](<Configuration/Environments Namespace/Environment Resolvers Namespace/Lakebase Resolver/Lakebase Resolver Branch Setting>).

This is the logical ID of the compute, not the hostname in the JDBC URL.

## Type

String

## Default

`primary`

## Usage

### Command-line

```bash
./flyway info -environments.development.resolvers.lakebase.compute='primary'
```

### TOML Configuration File

```toml
[environments.development.resolvers.lakebase]
compute = "primary"
```
