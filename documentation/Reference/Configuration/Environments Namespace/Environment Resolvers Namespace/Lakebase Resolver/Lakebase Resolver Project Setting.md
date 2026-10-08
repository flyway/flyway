---
subtitle: environments.*.resolvers.lakebase.project
---

## Description

The ID of the Lakebase project to request a database credential for.

This is the project's logical ID, not the hostname in the JDBC URL. Together with
[`branch`](<Configuration/Environments Namespace/Environment Resolvers Namespace/Lakebase Resolver/Lakebase Resolver Branch Setting>)
and
[`compute`](<Configuration/Environments Namespace/Environment Resolvers Namespace/Lakebase Resolver/Lakebase Resolver Compute Setting>)
it identifies the compute endpoint the credential is issued against.

Required when resolving `${lakebase.token}`, but not `${lakebase.user}`, which is workspace-wide.

## Type

String

## Default

<i>none</i>

## Usage

### Command-line

```bash
./flyway info -environments.development.resolvers.lakebase.project='my-project'
```

### TOML Configuration File

```toml
[environments.development.resolvers.lakebase]
project = "my-project"
```
