---
pill: scriptPlaceholderSuffix
subtitle: flyway.scriptPlaceholderSuffix
---

## Description

The suffix of every [script migration placeholder](https://documentation.red-gate.com/flyway/flyway-concepts/migrations/migration-placeholders)

## Type

String

## Default

`"__"`

## Usage

### Flyway Desktop

This can't be configured via Flyway Desktop, although it will be honoured.

### Command-line

```powershell
./flyway -scriptPlaceholderSuffix="__P" info
```

### TOML Configuration File

```toml
[flyway]
scriptPlaceholderSuffix = "__P"
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
