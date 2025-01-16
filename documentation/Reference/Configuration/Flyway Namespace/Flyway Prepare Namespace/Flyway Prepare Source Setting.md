---
subtitle: flyway.prepare.source
---

## Description

The source to use for the prepare operation.

## Type

String

### Valid values

- `"<<env>>"` - uses the environment named \<\<env>>
- `"schemaModel"` - the schema model folder referenced by [
  `schemaModelLocation`](<Configuration/Flyway Namespace/Flyway Schema Model Location Setting>)

In the case where an environment shares a name with one of the other values, it can be prefixed with `env:`

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
source = "schemaModel"
```
