---
subtitle: flyway.migrate.saveSnapshot
---

## Description

Takes a [snapshot](https://documentation.red-gate.com/flyway/flyway-concepts/snapshots) of the target database on
migrate success and stores it. The resulting snapshot will be stored in
the [snapshot history table](<Configuration/Flyway Namespace/Flyway Snapshot Namespace/Flyway Snapshot History Table Setting>)
in the target database by default, although this can be configured using
[`snapshot.filename`](<Configuration/Flyway Namespace/Flyway Snapshot Namespace/Flyway Snapshot Filename Setting>).
This setting is equivalent to running the [snapshot command](<Commands/Snapshot>) after the current migrate command.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an
advanced parameter in operations on the Migrations page.

### Command-line

```bash
./flyway migrate -saveSnapshot=true
```

### TOML Configuration File

This would usually be set over the command-line. It can be set in the toml, although it would apply to all environments
which might not be desirable.

```toml
[flyway.migrate]
saveSnapshot = true
```
