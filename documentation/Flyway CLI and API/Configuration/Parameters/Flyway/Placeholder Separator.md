---
pill: placeholderSeparator
subtitle: flyway.placeholderSeparator
redirect_from: Configuration/PlaceholdersSeparator/
---

# Placeholder Separator

## Description
The separator of default [placeholders](Configuration/Placeholders Configuration)

## Default
:

## Usage

### Commandline
```powershell
./flyway -placeholderSeparator="_" info
```

### TOML Configuration File
```toml
[flyway]
placeholderSeparator = "_"
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
