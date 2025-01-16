---
subtitle: redgateCompare.sqlserver.options.behavior.ignoreMigrationScripts
---

## Description

When this option is selected, SQL Source Control migration scripts will not be considered in comparisons

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
ignoreMigrationScripts = true
```
