---
subtitle: flyway.prepare.target
---

## Description

The target environment to deploy to.
Must match the id of an environment specified in [environments](<Configuration/Environments Namespace>).

## Type

String

## Default

<i>none - if `prepare.source` or `prepare.target` are not set the
`prepare` command will run against a [diff artifact](<Configuration/Flyway Namespace/Flyway Prepare Namespace/Flyway Prepare Artifact Filename Setting>)</i>

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
