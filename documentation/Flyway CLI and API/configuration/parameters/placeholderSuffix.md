---
layout: documentation
menu: configuration
pill: placeholderSuffix
subtitle: flyway.placeholderSuffix
redirect_from: /documentation/configuration/placeholderSuffix/
---

# Placeholder Suffix

## Description
The suffix of every [placeholder](/documentation/configuration/placeholder)

## Default
}

## Usage

### Commandline
```powershell
./flyway -placeholderSuffix="$$" info
```

### Configuration File
```properties
flyway.placeholderSuffix=$$
```

### Environment Variable
```properties
FLYWAY_PLACEHOLDER_SUFFIX=$$
```

### API
```java
Flyway.configure()
    .placeholderSuffix("$$")
    .load()
```

### Gradle
```groovy
flyway {
    placeholderSuffix = '$$'
}
```

### Maven
```xml
<configuration>
    <placeholderSuffix>$$</placeholderSuffix>
</configuration>
```