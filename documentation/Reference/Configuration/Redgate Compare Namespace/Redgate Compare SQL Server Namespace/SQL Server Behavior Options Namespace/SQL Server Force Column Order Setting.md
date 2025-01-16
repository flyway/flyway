---
subtitle: redgateCompare.sqlserver.options.behavior.forceColumnOrder
---

## Description

If additional columns are inserted into the middle of a table, this option forces a rebuild of the table so the column order is correct following deployment. Data will be preserved.

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
forceColumnOrder = true
```
