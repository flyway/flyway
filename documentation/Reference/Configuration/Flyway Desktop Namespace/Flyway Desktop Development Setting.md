---
subtitle: flywayDesktop.development
---

## Description

The name of your [development environment](https://documentation.red-gate.com/display/FD/Development+environment).
This corresponds to an environment id in your list of [environments](<Configuration/Environments Namespace>).

## Type

String

## Default

`"development"`

## Usage

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

This will always be set to the default value when a project is created using Flyway Desktop, though Flyway Desktop will
honour it if it is manually changed in the settings file.
If there is no environment with the given name, Flyway Desktop will prompt for connection details.

### TOML Configuration File

```toml
[flywayDesktop]
development = "development"
```
