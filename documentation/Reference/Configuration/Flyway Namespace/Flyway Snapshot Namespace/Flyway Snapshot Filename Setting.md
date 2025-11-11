---
subtitle: flyway.snapshot.filename
---

{% include enterprise.html %}

## Description

Location used to store the snapshot as a result of the [Snapshot](Commands/Snapshot) command being invoked.
The location type is determined by its prefix, and will be a file path if no prefix is specified.

### Filesystem

Locations starting with <code>filesystem:</code> (or with no prefix) point to a file path on the filesystem.
Relative paths will be resolved against your [working directory](<Command-line Parameters/Working Directory Parameter>).

### Snapshot History table

Locations starting with <code>snapshotHistory:</code> point to
the [snapshot history table](<Configuration/Flyway Namespace/Flyway Snapshot Namespace/Flyway Snapshot History Table Setting>).
They are in the format <code>snapshotHistory:&lt;name&gt;</code>. The name is an optional field stored in the table
which can be used to help look up snapshots if there are multiple entries (
see [snapshot history limit](<Configuration/Flyway Namespace/Flyway Snapshot Namespace/Flyway Snapshot History Limit Setting>)).
If the name is set to <code>current</code> or <code>previous</code>, it will be left blank, as these are treated as
keywords when looking up snapshots.

## Default

<i>none - this is a required parameter for the `snapshot` command</i>

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### Command-line

```powershell
./flyway snapshot -filename=flyway_snapshot.snp
```

### TOML Configuration File

```toml
[flyway.snapshot]
filename = "flyway_snapshot.snp"
```

### Configuration File

```properties
flyway.snapshot.filename=flyway_snapshot.snp
```

### Environment Variable

```properties
FLYWAY_SNAPSHOT_FILENAME=flyway_snapshot.snp
```

## Notes

The file extension `.snp` is not required, it is a convenience to help identify the file
