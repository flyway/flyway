---
subtitle: flyway.deploy.saveSnapshot
---

## Description

Takes a [snapshot](https://documentation.red-gate.com/flyway/flyway-concepts/snapshots) of the target database on
deployment success and stores it.
The resulting snapshot will be stored in the snapshot history table in the target database by default, although this can
be configured using
[`snapshot.filename`](<Configuration/Flyway Namespace/Flyway Snapshot Namespace/Flyway Snapshot Filename Setting>).
This setting is equivalent to running the [snapshot command](<Commands/Snapshot>) after the current deploy command.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### Command-line

```bash
./flyway deploy -saveSnapshot=true
```

### TOML Configuration File

```toml
[flyway.deploy]
saveSnapshot = true
```
