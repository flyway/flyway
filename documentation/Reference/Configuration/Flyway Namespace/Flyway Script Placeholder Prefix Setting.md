---
subtitle: flyway.scriptPlaceholderPrefix
---

## Description

The prefix of every [script migration placeholder](https://documentation.red-gate.com/flyway/flyway-concepts/migrations/migration-placeholders)

## Type

String

## Default

`"FP__"`

## Usage

### Flyway Desktop

This can't be configured via Flyway Desktop, although it will be honoured.

### Command-line

```powershell
./flyway -scriptPlaceholderPrefix="P__" info
```

### TOML Configuration File

```toml
[flyway]
scriptPlaceholderPrefix = "P__"
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
