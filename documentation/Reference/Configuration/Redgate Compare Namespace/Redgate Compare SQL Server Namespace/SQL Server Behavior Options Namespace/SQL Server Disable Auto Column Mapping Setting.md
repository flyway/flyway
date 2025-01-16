---
subtitle: redgateCompare.sqlserver.options.behavior.disableAutoColumnMapping
---

## Description

When this option is selected, similarly named columns in mapped tables will not be automatically mapped.

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
disableAutoColumnMapping = true
```
