---
subtitle: redgateCompare.sqlserver.options.ignores.ignoreStatisticsNoRecomputePropertyOnIndexes
---

## Description

Ignores the `STATISTICS_NORECOMPUTE` option on indexes and primary keys.

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
ignoreStatisticsNoRecomputePropertyOnIndexes = true
```
