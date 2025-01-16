---
subtitle: flyway.snapshot.source
---

## Description

The source from which a snapshot should be generated.

## Type

String

### Valid values

- `"<<env>>"` - uses the environment named \<\<env>>
- `"empty"` - models an empty database
- `"schemaModel"` - the schema model folder referenced by [
  `schemaModelLocation`](<Configuration/Flyway Namespace/Flyway Schema Model Location Setting>)
-

`"migrations"` - uses a buildEnvironment to represent the state of database after specified migrations have been applied

In the case where an environment shares a name with one of the other values, it can be prefixed with `env:`

## Default

Falls back to the current [`environment`](<Configuration/Flyway Namespace/Flyway Environment Setting>)

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### Command-line

```bash
./flyway snapshot -source="production"
```

### TOML Configuration File

```toml
[flyway.snapshot]
source = "production"
```
