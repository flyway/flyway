---
layout: documentation
menu: configuration
pill: cleanDisabled
subtitle: flyway.cleanDisabled
redirect_from: /documentation/configuration/cleanDisabled/
---

# Clean Disabled

## Description
Whether to disable clean. This is especially useful for production environments where running clean can be a career limiting move. Set to `false` to allow `clean` to execute.

## Default
true

## Usage

### Commandline
```powershell
./flyway -cleanDisabled="false" clean
```

### Configuration File
```properties
flyway.cleanDisabled=false
```

### Environment Variable
```properties
FLYWAY_CLEAN_DISABLED=false
```

### API
```java
Flyway.configure()
    .cleanDisabled(false)
    .load()
```

### Gradle
```groovy
flyway {
    cleanDisabled = false
}
```

### Maven
```xml
<configuration>
    <cleanDisabled>false</cleanDisabled>
</configuration>
```