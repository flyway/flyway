---
subtitle: flyway.snapshot.historyLimit
---

{% include enterprise.html %}

## Description

The maximum number of entries to store in the Flyway [snapshot history table](<Configuration/Flyway Namespace/Flyway Snapshot Namespace/Flyway Snapshot History Table Setting>).

For more information on the concept of the snapshot history table, see [Snapshots](https://documentation.red-gate.com/flyway/flyway-concepts/snapshots).

## Default

`5`

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### Command-line

```powershell
./flyway snapshot -historyLimit=10
```

### TOML Configuration File

```toml
[flyway.snapshot]
historyLimit = 10
```

### Configuration File

```properties
flyway.snapshot.historyLimit=10
```

### Environment Variable

Not available
