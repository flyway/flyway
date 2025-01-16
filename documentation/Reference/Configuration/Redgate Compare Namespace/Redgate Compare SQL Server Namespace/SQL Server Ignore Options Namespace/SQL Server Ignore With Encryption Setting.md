---
subtitle: redgateCompare.sqlserver.options.ignores.ignoreWithEncryption
---

## Description

Ignore `WITH ENCRYPTION` statements on triggers, views, stored procedures and functions.

This option overrides Add `WITH ENCRYPTION`.

## Type

Boolean

## Default

`false`

## Usage

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.ignores]
ignoreWithEncryption = true
```
