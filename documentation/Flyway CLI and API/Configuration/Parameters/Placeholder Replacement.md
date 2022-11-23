---
pill: placeholderReplacement
subtitle: flyway.placeholderReplacement
redirect_from: Configuration/PlaceholdersReplacement/
---

# Placeholder Replacement

## Description
Whether [placeholders](Configuration/Placeholders Configuration) should be replaced

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
