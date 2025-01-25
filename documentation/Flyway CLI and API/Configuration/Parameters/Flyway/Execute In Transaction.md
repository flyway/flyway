---
pill: executeInTransaction
subtitle: flyway.executeInTransaction
---

# Execute In Transaction

## Description
Whether Flyway should execute SQL within a transaction. <br/>

_Note: This parameter does [not apply to Native Connectors](https://documentation.red-gate.com/display/FD/Flyway+Native+Connectors+-+MongoDB)._

## Default
true

## Usage

### Commandline
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