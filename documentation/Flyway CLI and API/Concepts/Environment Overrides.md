---
subtitle: Environment Overrides
redirect_from: /documentation/environmentoverrides/
---
# Environment Overrides

This concept page assumes you understand the following areas of Flyway:
- [TOML Configuration File](<Configuration/TOML Configuration File>)
- [Environments](Configuration/parameters/Environments)
- [Flyway](Configuration/parameters/Flyway)

If you have not done so, please review these pages first.

## Why is this useful ?
There are parameters that can be defined for all of Flyway however there are instances where you may want more fine-grained control to specify this for a specific [environment](Configuration/parameters/Environments). 

For example, changing [`cleanDisabled`](<Configuration/parameters/flyway/clean disabled>) may be appropriate for your test environment but not for your production environment.  

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

# Configure via Command-line

The Environment Overrides feature can also be configured via the command-line. For example:

`-environments.test.flyway.locations=location4` 

Note - this will take precedence over other configuration, resulting in the actual location being set to `location4`

This is due to the [CLI Configuration Order](<Configuration/CLI Configuration Order>)

# Environment Variable Support

Environment Overrides do not have dedicated environment variable support

## Exceptions

The following are not configurable via Environments:
- [Environment](Configuration/parameters/Flyway/Environment)
- [Check](Configuration/parameters/Flyway/Check)
- [Cherry Pick](<Configuration/parameters/Flyway/Cherry Pick>)
- [Dapr Secrets](<Configuration/parameters/Flyway/Dapr Secrets>)
- [Dapr URL](<Configuration/parameters/Flyway/Dapr URL>)
- [Email](Configuration/parameters/Flyway/Email)
- [Token](Configuration/parameters/Flyway/Token)
- [Google Cloud Secret Manager Project](<Configuration/parameters/Flyway/Google Cloud Secret Manager Project>)
- [Google Cloud Secret Manager Secrets](<Configuration/parameters/Flyway/Google Cloud Secret Manager Secrets>)
- [License Key](<Configuration/parameters/Flyway/License Key>)
- [Oracle](Configuration/parameters/Flyway/Oracle)
- [PostgreSQL](Configuration/parameters/Flyway/PostgreSQL)
- [SQL Server](<Configuration/parameters/Flyway/SQL Server>)
- [Undo SQL Migration Prefix](<Configuration/parameters/Flyway/Undo SQL Migration Prefix>)
- [Vault Secrets](<Configuration/parameters/Flyway/Vault Secrets>)
- [Vault Token](<Configuration/parameters/Flyway/Vault Token>)
- [Vault URL](<Configuration/parameters/Flyway/Vault URL>)

Any Command-line only configuration parameters, including 
- [Working Directory](<Configuration/parameters/Flyway/Working Directory>)
- [Config Files](<Configuration/parameters/Flyway/Config Files>)
- [Config File Encoding](<Configuration/parameters/Flyway/Config File Encoding>)
- [Output Type](Configuration/parameters/Flyway/OutputType)

## Note

Parameters that are already part of the [environment namespace](Configuration/parameters/Environments) cannot be overridden as they already exist exclusively in that environment (for example `url` or `schemas`).

