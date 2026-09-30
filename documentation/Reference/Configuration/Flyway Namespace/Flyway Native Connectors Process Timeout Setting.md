---
subtitle: flyway.nativeConnectors.processTimeout
---

## Description

The maximum time, in seconds, to wait for an external migration process started by a Native Connector.
For MongoDB JavaScript migrations, this limits each `mongosh` migration process. Increase it for migrations
that need more than five minutes to complete.

This setting does not affect MongoDB JSON migrations or the timeout used to check connectivity.

## Type

Integer greater than zero

## Default

`300` (five minutes)

## Usage

### Command-line

```powershell
./flyway -nativeConnectors.processTimeout=900 migrate
```

### TOML Configuration File

```toml
[flyway.nativeConnectors]
processTimeout = 900
```

### Configuration File

```properties
flyway.nativeConnectors.processTimeout=900
```

### Environment Variable

```properties
FLYWAY_NATIVE_CONNECTORS_PROCESS_TIMEOUT=900
```

### API

```java
NativeConnectorsConfigurationExtension configurationExtension =
    configuration.getConfigurationExtension(NativeConnectorsConfigurationExtension.class);
configurationExtension.setProcessTimeout(900);
```

`NativeConnectorsConfigurationExtension` is in the `org.flywaydb.nc` package of `flyway-nc-core`.
