---
subtitle: redgateCompare.sqlserver.options.ignores.ignoreWithNoCheck
---

## Description

Ignores the `WITH NOCHECK` argument on foreign keys and check constraints.

When this option is selected, disabled constraints are ignored, so this option is useful if you want to find out if a constraint is disabled.

## Type

Boolean

## Default

`true`

## Usage

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.ignores]
ignoreWithNoCheck = true
```
