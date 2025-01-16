---
pill: placeholderReplacement
subtitle: flyway.placeholderReplacement
redirect_from: Configuration/PlaceholdersReplacement/
---

## Description

Whether [placeholders](https://documentation.red-gate.com/flyway/flyway-concepts/migrations/migration-placeholders) should be replaced.

Note that this setting can be set from [Script Configuration](<Script Configuration>) in addition to project configuration.

## Type

Boolean

## Default

`true`

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
./flyway -placeholderReplacement="false" info
```

### TOML Configuration File

```toml
[flyway]
placeholderReplacement = false
```

### Configuration File

```properties
flyway.placeholderReplacement=false
```

### Script Configuration File

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
