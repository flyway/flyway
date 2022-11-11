---
layout: documentation
menu: configuration
pill: connectRetries
subtitle: flyway.connectRetries
redirect_from: /documentation/configuration/connectRetries/
---

# Connect Retries

## Description
The maximum number of retries when attempting to connect to the database. After each failed attempt, Flyway will wait 1 second before attempting to connect again, up to the maximum number of times specified by connectRetries. The interval between retries doubles with each subsequent attempt.

## Default
0

## Usage

### Commandline
```powershell
./flyway -connectRetries=10 info
```

### Configuration File
```properties
flyway.connectRetries=10
```

### Environment Variable
```properties
FLYWAY_CONNECT_RETRIES=10
```

### API
```java
Flyway.configure()
    .connectRetries(10)
    .load()
```

### Gradle
```groovy
flyway {
    connectRetries = 10
}
```

### Maven
```xml
<configuration>
    <connectRetries>10</connectRetries>
</configuration>
```