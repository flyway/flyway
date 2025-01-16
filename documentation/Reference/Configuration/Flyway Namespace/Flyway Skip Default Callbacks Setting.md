---
pill: skipDefaultCallbacks
subtitle: flyway.skipDefaultCallbacks
redirect_from: Configuration/skipDefaultCallbacks/
---

## Description

Whether default built-in callbacks (sql) should be skipped. If true, only [custom callbacks](<Configuration/Flyway Namespace/Flyway callbacks Setting>) are used.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
. Namespace/Flyway callbacks) are used.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
. flyway -skipDefaultCallbacks="true" info
```

### TOML Configuration File

```toml
[flyway]
skipDefaultCallbacks = true
```

### Configuration File

```properties
flyway.skipDefaultCallbacks=true
```

### Environment Variable

```properties
FLYWAY_SKIP_DEFAULT_CALLBACKS=true
```

### API

```java
Flyway.configure()
    .skipDefaultCallbacks(true)
    .load()
```

### Gradle

```groovy
flyway {
    skipDefaultCallbacks = true
}
```

### Maven

```xml
<configuration>
    <skipDefaultCallbacks>true</skipDefaultCallbacks>
</configuration>
```
