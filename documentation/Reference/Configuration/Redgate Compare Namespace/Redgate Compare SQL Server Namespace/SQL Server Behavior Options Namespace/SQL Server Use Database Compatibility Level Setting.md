---
subtitle: redgateCompare.sqlserver.options.behavior.useDatabaseCompatibilityLevel
---

## Description

Uses a database's compatibility level instead of the SQL Server version.

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
[redgateCompare.sqlserver.options.behavior]
useDatabaseCompatibilityLevel = true
```
