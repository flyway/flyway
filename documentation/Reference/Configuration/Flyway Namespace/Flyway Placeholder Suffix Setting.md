---
pill: placeholderSuffix
subtitle: flyway.placeholderSuffix
redirect_from: Configuration/PlaceholdersSuffix/
---

## Description

The suffix of every [placeholder](https://documentation.red-gate.com/flyway/flyway-concepts/migrations/migration-placeholders)

## Type

String

## Default

`"}"`

## Usage

### Flyway Desktop

This can't be configured via Flyway Desktop, although it will be honoured.

### Command-line

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
