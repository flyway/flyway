---
subtitle: redgateCompare.sqlserver.options.ignores.ignoreStatistics
---

## Description

Ignores statistics when comparing and deploying databases. Objects will be written to the schema model without statistics even if the development database uses statistics.

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
ignoreStatistics = true
```
