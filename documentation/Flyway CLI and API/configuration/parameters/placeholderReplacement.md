---
layout: documentation
menu: configuration
pill: placeholderReplacement
subtitle: flyway.placeholderReplacement
redirect_from: /documentation/configuration/placeholderReplacement/
---

# Placeholder Replacement

## Description
Whether [placeholders](/documentation/configuration/placeholder) should be replaced

## Default
true

## Usage

### Commandline
```powershell
./flyway -placeholderReplacement="false" info
```

### Configuration File
```properties
flyway.placeholderReplacement=false
```

### Environment Variable
```properties
FLYWAY_PLACEHOLDER_REPLACEMENT=false
```

### API
```java
Flyway.configure()
    .placeholderReplacement(false)
    .load()
```

### Gradle
```groovy
flyway {
    placeholderReplacement = false
}
```

### Maven
```xml
<configuration>
    <placeholderReplacement>false</placeholderReplacement>
</configuration>
```