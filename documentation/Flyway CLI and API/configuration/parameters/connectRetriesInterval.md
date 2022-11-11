---
layout: documentation
menu: configuration
pill: connectRetriesInterval
subtitle: flyway.connectRetriesInterval
---

# Connect Retries Interval

## Description
The maximum time between retries when attempting to connect to the database in seconds. This will cap the interval between connect retries to the value provided.

## Default
120

## Usage

### Commandline
```powershell
./flyway -connectRetriesInterval=60 info
```

### Configuration File
```properties
flyway.connectRetriesInterval=60
```

### Environment Variable
```properties
FLYWAY_CONNECT_RETRIES_INTERVAL=60
```

### API
```java
Flyway.configure()
    .connectRetriesInterval(60)
    .load()
```

### Gradle
```groovy
flyway {
    connectRetriesInterval = 60
}
```

### Maven
```xml
<configuration>
    <connectRetriesInterval>60</connectRetriesInterval>
</configuration>
```