---
pill: placeholderSuffix
subtitle: flyway.placeholderSuffix
redirect_from: Configuration/PlaceholdersSuffix/
---

# Placeholder Suffix

## Description
The suffix of every [placeholder](Configuration/Placeholders Configuration)

## Default
}

## Usage

### Commandline
```powershell
./flyway -placeholderSuffix="$$" info
```

### TOML Configuration File
```toml
[flyway]
placeholderSuffix = "$$"
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
