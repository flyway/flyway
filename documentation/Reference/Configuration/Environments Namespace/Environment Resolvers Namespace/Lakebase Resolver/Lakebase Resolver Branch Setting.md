---
subtitle: environments.*.resolvers.lakebase.branch
---

## Description

The ID of the branch within the Lakebase
[`project`](<Configuration/Environments Namespace/Environment Resolvers Namespace/Lakebase Resolver/Lakebase Resolver Project Setting>)
that owns the compute endpoint to request a database credential for.

## Type

String

## Default

`production`

## Usage

### Command-line

```bash
./flyway info -environments.development.resolvers.lakebase.branch='production'
```

### TOML Configuration File

```toml
[environments.development.resolvers.lakebase]
branch = "production"
```
