---
pill: placeholderSeparator
subtitle: flyway.placeholderSeparator
redirect_from: Configuration/PlaceholdersSeparator/
---

## Description

The separator of default [placeholders](https://documentation.red-gate.com/flyway/flyway-concepts/migrations/migration-placeholders)

## Type

String

## Default

`":"`

## Usage

### Flyway Desktop

This can't be configured via Flyway Desktop, although it will be honoured.

### Command-line

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
