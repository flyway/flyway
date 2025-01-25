---
pill: group
subtitle: flyway.group
redirect_from: Configuration/group/
---

# Group

## Description
Whether to group all pending migrations together in the same transaction when applying them (only recommended for databases with support for DDL transactions)

_Note_: 
- _If [executeInTransaction](<Configuration/Parameters/Flyway/Execute In Transaction>) is set to false, this parameter will have no impact._
- _This parameter does not apply to [callbacks](<Concepts/Callback concept>), which cannot be included in the same transaction._
- _This parameter does not apply to Native Connectors, as they [do not support transactions](https://documentation.red-gate.com/display/FD/Flyway+Native+Connectors+-+MongoDB)._

## Default
false

## Usage

### Commandline
```powershell
./flyway -group="true" info
```

### TOML Configuration File
```toml
[flyway]
group = true
```

### Configuration File
```properties
flyway.group=true
```

### Environment Variable
```properties
FLYWAY_GROUP=true
```

### API
```java
Flyway.configure()
    .group(true)
    .load()
```

### Gradle
```groovy
flyway {
    group = true
}
```

### Maven
```xml
<configuration>
    <group>true</group>
</configuration>
```
