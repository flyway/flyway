---
layout: documentation
menu: configuration
pill: placeholderPrefix
subtitle: flyway.placeholderPrefix
redirect_from: /documentation/configuration/placeholderPrefix/
---

# Placeholder Prefix

## Description
The prefix of every [placeholder](/documentation/configuration/placeholder)

## Default
${

## Usage

### Commandline
```powershell
./flyway -placeholderPrefix="$$" info
```

### Configuration File
```properties
flyway.placeholderPrefix=$$
```

### Environment Variable
```properties
FLYWAY_PLACEHOLDER_PREFIX=$$
```

### API
```java
Flyway.configure()
    .placeholderPrefix("$$")
    .load()
```

### Gradle
```groovy
flyway {
    placeholderPrefix = '$$'
}
```

### Maven
```xml
<configuration>
    <placeholderPrefix>$$</placeholderPrefix>
</configuration>
```