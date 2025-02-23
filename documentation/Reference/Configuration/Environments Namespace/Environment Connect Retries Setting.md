---
pill: connectRetries
subtitle: flyway.environments.*.connectRetries
redirect_from: Configuration/connectRetries/
---

## Description

The maximum number of retries when attempting to connect to the database.
After each failed attempt, Flyway will wait 1 second before attempting to connect again, up to the maximum number of times specified by `connectRetries`. The interval between retries doubles with each subsequent attempt.

## Type

Integer

## Default

`0`

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
./flyway -connectRetries=10 info
```

To configure a named environment via command line when using a TOML configuration, prefix `connectRetries` with
`environments.{environment name}.` for example:

```powershell
./flyway -environments.sample.connectRetries=10 info
```

### TOML Configuration File

```toml
[environments.default]
connectRetries = 10
```

### Configuration File

```properties
flyway.connectRetries=10
```

### Environment Variable

```properties
FLYWAY_CONNECT_RETRIES=10
```

### API

```java
Flyway.configure()
    .connectRetries(10)
    .load()
```

### Gradle

```groovy
flyway {
    connectRetries = 10
}
```

### Maven

```xml
<configuration>
    <connectRetries>10</connectRetries>
</configuration>
```
