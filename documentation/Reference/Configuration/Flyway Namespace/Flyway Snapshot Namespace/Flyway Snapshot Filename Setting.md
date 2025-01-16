---
pill: snapshot.filename
subtitle: flyway.snapshot.filename
---

{% include enterprise.html %}

{% include commandlineonly.html %}

## Description

Filename used to store the snapshot as a result of the [Snapshot](Commands/Snapshot) command being invoked

## Default

<i>none - this is a required parameter for the `snapshot` command</i>

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### Command-line

```powershell
./flyway -snapshot.filename=flyway_snapshot.snp snapshot
```

### TOML Configuration File

```toml
[flyway]
snapshot.filename = "flyway_snapshot.snp"
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
