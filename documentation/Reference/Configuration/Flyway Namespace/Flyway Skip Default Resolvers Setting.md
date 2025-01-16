---
pill: skipDefaultResolvers
subtitle: flyway.skipDefaultResolvers
redirect_from: Configuration/skipDefaultResolvers/
---

## Description

Whether default built-in resolvers (sql and jdbc) should be skipped. If `true`, only [custom resolvers](<Configuration/Flyway Namespace/Flyway Migration Resolvers Setting>) are used.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
./flyway -skipDefaultResolvers="true" info
```

### TOML Configuration File

```toml
[flyway]
skipDefaultResolvers = true
```

### Configuration File

```properties
flyway.skipDefaultResolvers=true
```

### Environment Variable

```properties
FLYWAY_SKIP_DEFAULT_RESOLVERS=true
```

### API

```java
Flyway.configure()
    .skipDefaultResolvers(true)
    .load()
```

### Gradle

```groovy
flyway {
    skipDefaultResolvers = true
}
```

### Maven

```xml
<configuration>
    <skipDefaultResolvers>true</skipDefaultResolvers>
</configuration>
```
