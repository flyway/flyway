---
layout: documentation
menu: configuration
pill: scriptPlaceholderPrefix
subtitle: flyway.scriptPlaceholderPrefix
---

# Script Placeholder Prefix
{% include teams.html %}

## Description
The prefix of every [script migration placeholder](/documentation/configuration/placeholder)

## Default
FP__

## Usage

### Commandline
```powershell
./flyway -scriptPlaceholderPrefix="P__" info
```

### Configuration File
```properties
flyway.scriptPlaceholderPrefix=P__
```

### Environment Variable
```properties
FLYWAY_SCRIPT_PLACEHOLDER_PREFIX=P__
```

### API
```java
Flyway.configure()
    .scriptPlaceholderPrefix("P__")
    .load()
```

### Gradle
```groovy
flyway {
    scriptPlaceholderPrefix = 'P__'
}
```

### Maven
```xml
<configuration>
    <scriptPlaceholderPrefix>P__</scriptPlaceholderPrefix>
</configuration>
```