---
subtitle: Environment Overrides
---

# Environment Overrides

This concept page assumes you understand the following areas of Flyway:
- [Environments Namespace](<Configuration/Environments Namespace>)
- [Flyway Namespace](<Configuration/Flyway Namespace>)

If you have not done so, please review these pages first.

## Why is this useful ?
There are parameters that can be defined globally for Flyway however there are instances where you may want more fine-grained control to specify this for a specific [environment](<Configuration/Flyway Namespace/Flyway Environment Setting>). 

For example, changing [`cleanDisabled`](<Configuration/Flyway Namespace/Flyway Clean Disabled Setting>) may be appropriate for your test environment but not for your production environment.  

## How is this used ?
Environments will support configuration overrides by adding a `flyway` table to your environment. For example:

```
[flyway]
environment = "test"
locations = ["location1"]

[environments.test]
url = "jdbc:\\..."

[environments.test.flyway]
locations = ["location2","location3"]
```

If the current environment has an override, its override will be used, in totality, over the root or default version.

To clarify, with a toml looking like the above example, Then locations will be `location1` unless using the `test` environment, in which case locations will be `location2` and `location3`.

## Configure via Command-line

The Environment Overrides feature can also be configured via the command-line. For example:

`-environments.test.flyway.locations=location4` 

Note - this will take precedence over other configuration, resulting in the actual location being set to `location4`

This is due to the [Configuration Precedence](<Configuration Precedence>)

## Environment Variable Support

Environment Overrides do not have dedicated environment variable support

## Parameters outside the Flyway namespace

Parameters that are not part of the [Flyway namespace](<Configuration/Flyway Namespace>), such as those in other namespaces like `check`, can also be overridden per environment. For example:

```
[flyway]
environment = "test"
check.buildEnvironment = "build"
check.filterFile = "filter.txt"

[environments.test.flyway]
check.buildEnvironment = "test-build"
```

The environment's values are merged over the root `[flyway]` values key by key, so any parameter not set in the environment keeps its root value. An unknown parameter under `environments.<name>.flyway` is reported as an error.

### Java API

Environment overrides for these parameters are applied when the configuration is loaded from TOML (for example by the command line). If you change the environment on an already-loaded configuration through the Java API, call `reloadPluginConfiguration()` straight after setting the environment, then set any values you need for these parameters:

```java
FluentConfiguration config = Flyway.configure()
    .environment("test")
    .reloadPluginConfiguration();
```

Reloading re-applies the values from the configuration, so any value you previously set directly on one of these parameters that the configuration names is discarded. Set such values after the reload.

This feature requires `flyway-core-utilities` on the classpath. Without it, the reload does nothing.

## Exceptions

The following are not configurable via Environments:
- Environment

## Not recommended

The following can be overridden per environment, but doing so is not recommended, as they identify you or your license rather than describe an environment:
- Email
- Token
- License Key

## Command-line only parameters

Command-line only configuration parameters cannot be set via Environments, including
- Working Directory
- Config Files
- Config File Encoding
- Output Type
- Color

## Note

Parameters that are already part of the [environment namespace](<Configuration/Environments Namespace>) cannot be overridden as they already exist exclusively in that environment (for example `url` or `schemas`).
