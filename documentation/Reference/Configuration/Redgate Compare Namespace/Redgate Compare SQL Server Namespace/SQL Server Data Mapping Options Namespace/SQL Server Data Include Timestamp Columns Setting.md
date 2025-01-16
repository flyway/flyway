---
subtitle: redgateCompare.sqlserver.data.options.mapping.includeTimestampColumns
---

## Description

Includes timestamp columns in the comparison. Timestamp columns can't be deployed.

## Type

Boolean

## Default

`false`

## Usage

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

This can be set from the data comparison options settings in SQL Server projects.

### TOML Configuration File

```toml
[redgateCompare.sqlserver.data.options.mapping]
includeTimestampColumns = true
```
