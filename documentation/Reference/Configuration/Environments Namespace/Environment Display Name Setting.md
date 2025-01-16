---
pill: displayName
subtitle: flyway.environments.*.displayName
---

## Description

The display name for the environment within Flyway Desktop.

This does not have the same character limitations as the environment id.

This setting does not affect the Flyway commandline in any way.

## Type

String

## Default

<i>none</i>

## Usage

### Flyway Desktop

This can be set from the connection dialog.

### TOML Configuration File

```toml
[environments.development]
displayName = "Development Database"
```
