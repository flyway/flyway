---
subtitle: flyway.check.nextSnapshot
---

{% include enterprise.html %}

## Description

A snapshot containing all migrations including those that are pending (generated via [`snapshot`](Commands/snapshot))
The location type is determined by its prefix, and will be a file path if no prefix is specified.

### Filesystem

Locations starting with <code>filesystem:</code> (or with no prefix) point to a file path on the filesystem.
Relative paths will be resolved against your [working directory](<Command-line Parameters/Working Directory Parameter>).

### Snapshot History table

Locations starting with <code>snapshotHistory:</code> point to
the [snapshot history table](<Configuration/Flyway Namespace/Flyway Snapshot Namespace/Flyway Snapshot History Table Setting>).
They are in the format <code>snapshotHistory:&lt;name&gt;</code>.
For more information on the concept of the snapshot history table,
see [Snapshots](https://documentation.red-gate.com/flyway/flyway-concepts/snapshots).

## Type

String

## Default

<i>none</i>

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an
advanced parameter in operations on the Migrations page.

### Command-line

```powershell
./flyway check -changes -url="jdbc://url" -nextSnapshot="my_snapshot"
```

### TOML Configuration File

```toml
[flyway.check]
nextSnapshot = "my_snapshot"
```

### Configuration File

```properties
flyway.check.nextSnapshot=my_snapshot
```