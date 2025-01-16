---
subtitle: Environment Variables
redirect_from: /documentation/envvars/
---

## Supplying configuration via environment variables

Flyway allows you to define most settings via environment variables.

See [configuration](<Configuration>) for a full list of supported configuration parameters. Each individual settings page will list the environment variable, if supported.
Some [commandline parameters](<Command-line Parameters>) can also be set via environment variables.

## List of environment variables

The following exist only as environment variables and have no corresponding configuration setting or command line parameter:

| Environment variable                                                                                  | Tier      | Description                                                |
|-------------------------------------------------------------------------------------------------------|-----------|------------------------------------------------------------|
| [`REDGATE_DISABLE_TELEMETRY`](<Environment Variables/Redgate Disable Telemetry Environment Variable>) | Community | Disable Flyway's telemetry client from sending usage data. |

## Configuration precedence

Environment variables take precedence over settings specified in config files, but are overridden by settings specified as command line parameters.
See [Configuration Order](<Configuration Precedence>).

## Accessing environment variables via the API 

This is possible using the Flyway API by calling the `envVars()` method on the configuration.
