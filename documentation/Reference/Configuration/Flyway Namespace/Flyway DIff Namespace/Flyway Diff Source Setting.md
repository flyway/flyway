---
subtitle: flyway.diff.source
---

## Description

The source to use for the diff operation.

## Type

String

### Valid values

- `"<<env>>"` - uses the environment named \<\<env>>
- `"empty"` - models an empty database
- `"schemaModel"` - the schema model folder referenced by [`schemaModelLocation`](<Configuration/Flyway Namespace/Flyway Schema Model Location Setting>)
- `"migrations"` - uses a buildEnvironment to represent the state of database after specified migrations have been applied
- `"snapshot:<<path>>"` - uses a snapshot file at the specified path
- `"snapshotHistory:<<name>>"` - uses a snapshot entry in the [snapshot history table](<Configuration/Flyway Namespace/Flyway Snapshot Namespace/Flyway Snapshot History Table Setting>); entries are either identified by name, or `current` for the most recent snapshot, or `previous` for the most recent but one. 

In the case where an environment shares a name with one of the other values, it can be prefixed with `env:`

## Default

<i>none - this is a required parameter for the `diff` command<i>

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### Command-line

```bash
./flyway diff -source="env:development" -target="schemaModel"
```

### TOML Configuration File

It is unlikely to be desirable to specify this in a config file, but it is possible:

```toml
[flyway.diff]
source = "env:development"
```
