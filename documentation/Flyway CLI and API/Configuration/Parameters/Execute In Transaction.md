---
pill: executeInTransaction
subtitle: flyway.executeInTransaction
---

# Execute In Transaction

## Description
Whether Flyway should execute SQL within a transaction. <br/>

## Default
true

## Usage

### Commandline
```powershell
./flyway -executeInTransaction="false" migrate
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