---
pill: snapshot.filename
subtitle: flyway.snapshot.filename
---
# Snapshot Filename
{% include enterprise.html %}

{% include commandlineonly.html %}
 
## Description
Filename used to store the snapshot as a result of the [Snapshot](Commands/Snapshot) command being invoked

## Default
None

## Usage

### Commandline
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
