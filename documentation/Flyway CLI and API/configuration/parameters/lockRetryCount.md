---
layout: documentation
menu: configuration
pill: lockRetryCount
subtitle: flyway.lockRetryCount
redirect_from: /documentation/configuration/lockRetryCount
---

# Lock Retry Count

## Description
At the start of a migration, Flyway will attempt to take a lock to prevent competing instances executing in parallel.
If this lock cannot be obtained straight away, Flyway will retry at 1s intervals, until this count is reached, at which
point it will abandon the migration. A value of -1 indicates that Flyway should keep retrying indefinitely.

## Default
<i>Retry 50 times then fail</i>

## Usage

### Commandline
```powershell
./flyway -lockRetryCount=10 migrate
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