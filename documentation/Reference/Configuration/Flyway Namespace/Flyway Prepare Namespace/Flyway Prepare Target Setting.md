---
subtitle: flyway.prepare.target
---

## Description

The target environment to deploy to.
Must match the id of an environment specified in [environments](<Configuration/Environments Namespace>).

## Type

String

## Default

- If [`prepare.source`](<Configuration/Flyway Namespace/Flyway Prepare Namespace/Flyway Prepare Source Setting>) is set to `schemaModel` or `migrations`, this defaults to the value of the [`environment`](<Configuration/Flyway Namespace/Flyway Environment Setting>) parameter.
- Otherwise, there is no default value. It must be set when preparing from a source database, and should not be set if preparing from an artifact.

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### Command-line

```bash
./flyway prepare -source="schemaModel" -target="production"
```

### TOML Configuration File

```toml
[flyway.prepare]
target = "production"
```
