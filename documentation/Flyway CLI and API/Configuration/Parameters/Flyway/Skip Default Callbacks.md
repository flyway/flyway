---
pill: skipDefaultCallbacks
subtitle: flyway.skipDefaultCallbacks
redirect_from: Configuration/skipDefaultCallbacks/
---

# Skip Default Callbacks

## Description
Whether default built-in callbacks (sql) should be skipped. If true, only [custom callbacks](Configuration/parameters/flyway/callbacks) are used.

## Default
false

## Usage

### Commandline
```powershell
./flyway -skipDefaultCallbacks="true" info
```

### TOML Configuration File
```toml
[flyway]
skipDefaultCallbacks = true
```

### Configuration File
```properties
flyway.skipDefaultCallbacks=true
```

### Environment Variable
```properties
FLYWAY_SKIP_DEFAULT_CALLBACKS=true
```

### API
```java
Flyway.configure()
    .skipDefaultCallbacks(true)
    .load()
```

### Gradle
```groovy
flyway {
    skipDefaultCallbacks = true
}
```

### Maven
```xml
<configuration>
    <skipDefaultCallbacks>true</skipDefaultCallbacks>
</configuration>
```
