---
subtitle: flyway.group
redirect_from: Configuration/group/
---

## Description

Whether to group all pending migrations together in the same transaction when applying them (only recommended for databases with support for DDL transactions)

_Note_:
- _If [executeInTransaction](<Configuration/Flyway Namespace/Flyway Execute In Transaction Setting>) is set to false, this parameter will have no impact._
- _This parameter does not apply to [callbacks](https://documentation.red-gate.com/flyway/flyway-concepts/callbacks), which can't be included in the same transaction._
- _This parameter does not apply to Native Connectors, as they [do not support transactions](https://documentation.red-gate.com/display/FD/Flyway+Native+Connectors+-+MongoDB)._

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

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
