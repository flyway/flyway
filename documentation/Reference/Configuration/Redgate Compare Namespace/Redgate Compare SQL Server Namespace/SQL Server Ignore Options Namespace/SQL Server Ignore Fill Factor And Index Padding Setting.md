---
subtitle: redgateCompare.sqlserver.options.ignores.ignoreFillFactorAndIndexPadding
---

## Description

Ignores the fill factor and index padding in indexes and primary keys when comparing and deploying databases.

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
ignoreFillFactorAndIndexPadding = true
```
