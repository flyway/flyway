---
subtitle: flyway.snapshot.historyTable
---

{% include enterprise.html %}

## Description

The name of the Flyway snapshot history table in which to store snapshots, e.g. for drift detection.

By default the snapshot history table is placed in the default schema for the connection provided by the datasource.
When the [`defaultSchema`](<Configuration/Flyway Namespace/Flyway Default Schema Setting>) or [`schemas`](<Configuration/Environments Namespace/Environment schemas Setting>) property is set, the snapshot history
table is placed in the specified default schema.

For more information on the concept of the snapshot history table, see [Snapshots](https://documentation.red-gate.com/flyway/flyway-concepts/snapshots).

## Default

`"flyway_snapshot_history"`

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### Command-line

```powershell
./flyway snapshot -historyTable=flyway_snapshot_history
```

### TOML Configuration File

```toml
[flyway.snapshot]
historyTable = "flyway_snapshot_history"
```

### Configuration File

```properties
flyway.snapshot.historyTable=flyway_snapshot_history
```

### Environment Variable

Not available
