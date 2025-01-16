---
pill: lockRetryCount
subtitle: flyway.lockRetryCount
redirect_from: Configuration/lockRetryCount
---

## Description

At the start of a migration, Flyway will attempt to take a lock to prevent competing instances executing in parallel.
If this lock can't be obtained straight away, Flyway will retry at 1s intervals, until this count is reached, at which
point it will abandon the migration. A value of -1 indicates that Flyway should keep retrying indefinitely.

_Note: Locking is [not currently implemented in Native Connectors](https://documentation.red-gate.com/display/FD/Flyway+Native+Connectors+-+MongoDB)._

## Type

Integer

## Default

`50`

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
./flyway -lockRetryCount=10 migrate
```

### TOML Configuration File

```toml
[flyway]
lockRetryCount = 10
```

### Configuration File

```properties
flyway.lockRetryCount=10
```

### Environment Variable

```properties
FLYWAY_LOCK_RETRY_COUNT=10
```

### API

```java
Flyway.configure()
    .lockRetryCount(10)
    .load()
```

### Gradle

```groovy
flyway {
    lockRetryCount = 10
}
```

### Maven

```xml
<configuration>
    <lockRetryCount>10</lockRetryCount>
</configuration>
```
