---
subtitle: flyway.environments.*.flyway
---

There are configuration settings that can be defined for all Flyway invocations. However, there are instances where you may want more fine-grained control to specify this for a specific [environment](Configuration/Environments Namespace).

For example, changing [`cleanDisabled`](<Configuration/Flyway Namespace/Flyway Clean Disabled Setting>) may be appropriate for your test environment but not for your production environment.

## Override exceptions

All settings in the [Flyway namespace](<Configuration/Flyway Namespace>) which can be configured in a TOML configuration file can be overridden at the environment level except the following:

Excluded settings:
- [Environment](<Configuration/Flyway Namespace/Flyway Environment Setting>)
- [Cherry Pick](<Configuration/Flyway Namespace/Flyway Cherry Pick Setting>)
- [Email](<Configuration/Flyway Namespace/Flyway Email Setting>)
- [Token](<Configuration/Flyway Namespace/Flyway Token Setting>)
- [License Key](<Configuration/Flyway Namespace/Flyway License Key Setting>)
- [Undo SQL Migration Prefix](<Configuration/Flyway Namespace/Flyway Undo SQL Migration Prefix Setting>)

Settings within child namespaces of the flyway namespace (such as [`flyway.oracle`](<Configuration/Flyway Namespace/Flyway Oracle Namespace>)) cannot be overridden.

## Usage examples

### Command-line

`-environments.test.flyway.locations=location4`

### TOML Configuration file

```
[flyway]
environment = "test"
locations = ["location1"]

[environments.test]
url = "jdbc:\\..."

[environments.test.flyway]
locations = ["location2","location3"]
```

### Environment Variable

<i>Environment overrides do not have dedicated environment variable support</i>