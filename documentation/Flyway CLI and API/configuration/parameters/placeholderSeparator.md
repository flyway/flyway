---
layout: documentation
menu: configuration
pill: placeholderSeparator
subtitle: flyway.placeholderSeparator
redirect_from: /documentation/configuration/placeholderSeparator/
---

# Placeholder Separator

## Description
The separator of default [placeholders](/documentation/configuration/placeholder)

## Default
:

## Usage

### Commandline
```powershell
./flyway -placeholderSeparator="_" info
```

### Configuration File
```properties
flyway.placeholderSeparator=_
```

### Environment Variable
```properties
FLYWAY_PLACEHOLDER_SEPARATOR=_
```

### API
```java
Flyway.configure()
    .placeholderSeparator("_")
    .load()
```

### Gradle
```groovy
flyway {
    placeholderSeparator = '_'
}
```

### Maven
```xml
<configuration>
    <placeholderSeparator>_</placeholderSeparator>
</configuration>
```