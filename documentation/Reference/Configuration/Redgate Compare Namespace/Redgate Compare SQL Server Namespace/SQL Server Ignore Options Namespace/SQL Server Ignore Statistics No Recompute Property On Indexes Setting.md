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

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.options.ignores.ignoreStatisticsNoRecomputePropertyOnIndexes=true
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.ignores]
ignoreStatisticsNoRecomputePropertyOnIndexes = true
```
