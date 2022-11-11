---
layout: documentation
menu: configuration
pill: scriptPlaceholderSuffix
subtitle: flyway.scriptPlaceholderSuffix
---

# Script Placeholder Suffix
{% include teams.html %}

## Description
The suffix of every [script migration placeholder](/documentation/configuration/placeholder)

## Default
__

## Usage

### Commandline
```powershell
./flyway -scriptPlaceholderSuffix="__P" info
```

### Configuration File
```properties
flyway.scriptPlaceholderSuffix=__P
```

### Environment Variable
```properties
FLYWAY_SCRIPT_PLACEHOLDER_SUFFIX=__P
```

### API
```java
Flyway.configure()
    .scriptPlaceholderSuffix("__P")
    .load()
```

### Gradle
```groovy
flyway {
    scriptPlaceholderSuffix = '__P'
}
```

### Maven
```xml
<configuration>
    <scriptPlaceholderSuffix>__P</scriptPlaceholderSuffix>
</configuration>
```