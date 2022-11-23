---
pill: placeholderPrefix
subtitle: flyway.placeholderPrefix
redirect_from: Configuration/PlaceholdersPrefix/
---

# Placeholder Prefix

## Description
The prefix of every [placeholder](Configuration/Placeholders Configuration)

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
