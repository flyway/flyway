---
pill: stream
subtitle: flyway.stream
redirect_from: Configuration/stream/
---

## Description

Whether to stream SQL migrations when executing them. Streaming doesn't load the entire migration in memory at once. Instead each statement is loaded individually.

This is particularly useful for very large SQL migrations composed of multiple MB or even GB of reference data, as this dramatically reduces Flyway's memory consumption.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
./flyway -stream="true" info
```

### TOML Configuration File

```toml
[flyway]
stream = true
```

### Configuration File

```properties
flyway.stream=true
```

### Environment Variable

```properties
FLYWAY_STREAM=true
```

### API

```java
Flyway.configure()
    .stream(true)
    .load()
```

### Gradle

```groovy
flyway {
    stream = true
}
```

### Maven

```xml
<configuration>
    <stream>true</stream>
</configuration>
```
