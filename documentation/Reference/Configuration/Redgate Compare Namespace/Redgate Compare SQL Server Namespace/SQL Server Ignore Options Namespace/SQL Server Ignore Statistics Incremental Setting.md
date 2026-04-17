---
subtitle: redgateCompare.sqlserver.options.ignores.ignoreStatisticsIncremental
---

## Description

Ignore Statistics Incremental during comparison.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.options.ignores.ignoreStatisticsIncremental=true
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.ignores]
ignoreStatisticsIncremental = true
```
