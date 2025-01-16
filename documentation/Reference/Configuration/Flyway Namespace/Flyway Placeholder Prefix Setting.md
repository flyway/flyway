---
pill: placeholderPrefix
subtitle: flyway.placeholderPrefix
redirect_from: Configuration/PlaceholdersPrefix/
---

## Description

The prefix of every [placeholder](https://documentation.red-gate.com/flyway/flyway-concepts/migrations/migration-placeholders)

## Type

String

## Default

`"${"`

## Usage

### Flyway Desktop

This can't be configured via Flyway Desktop, although it will be honoured.

### Command-line

```powershell
./flyway -placeholderPrefix="$$" info
```

### TOML Configuration File

```toml
[flyway]
placeholderPrefix = "$$"
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
