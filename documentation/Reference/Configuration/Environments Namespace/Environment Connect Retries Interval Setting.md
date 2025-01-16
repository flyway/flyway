---
pill: connectRetriesInterval
subtitle: flyway.environments.*.connectRetriesInterval
---

## Description

The maximum time between retries when attempting to connect to the database in seconds.
This will cap the interval between connect retries to the value provided.

## Type

Integer

## Default

`120`

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
./flyway -connectRetriesInterval=60 info
```

To configure a named environment via command line when using a TOML configuration, prefix `connectRetriesInterval` with
`environments.{environment name}.` for example:

```powershell
./flyway -environments.sample.connectRetriesInterval=60 info
```

### TOML Configuration File

```toml
[environments.default]
connectRetriesInterval = 60
```

### Configuration File

```properties
flyway.connectRetriesInterval=60
```

### Environment Variable

```properties
FLYWAY_CONNECT_RETRIES_INTERVAL=60
```

### API

```java
Flyway.configure()
    .connectRetriesInterval(60)
    .load()
```

### Gradle

```groovy
flyway {
    connectRetriesInterval = 60
}
```

### Maven

```xml
<configuration>
    <connectRetriesInterval>60</connectRetriesInterval>
</configuration>
```
