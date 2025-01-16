---
subtitle: redgateCompare.sqlserver.options.ignores.ignoreCollations
---

## Description

Ignores collations on character data type columns when comparing and deploying databases.

This option is not used for memory-optimized tables.

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
ignoreCollations = true
```
