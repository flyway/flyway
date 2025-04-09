---
subtitle: flyway.executeInTransaction
---

## Description

Whether Flyway should execute SQL within a transaction. <br/>

Note that this setting can be set from [Script Configuration](<Script Configuration>) in addition to project configuration.

_Note: This parameter does [not apply to Native Connectors](https://documentation.red-gate.com/display/FD/Flyway+Native+Connectors+-+MongoDB)._

## Type

Boolean

## Default

`true`

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
./flyway -executeInTransaction="false" migrate
```

### TOML Configuration File

```toml
[flyway]
executeInTransaction = false
```

### Configuration File

```properties
flyway.executeInTransaction=false
```

### Script Configuration File

```properties
flyway.executeInTransaction=false
```

### Environment Variable

```properties
FLYWAY_EXECUTE_IN_TRANSACTION=false
```

### API

```java
Flyway.configure()
    .executeInTransaction(false)
    .load()
```

### Gradle

Not available

### Maven

Not available