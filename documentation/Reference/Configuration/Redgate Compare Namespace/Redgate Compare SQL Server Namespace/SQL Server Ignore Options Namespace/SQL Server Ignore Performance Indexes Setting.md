---
subtitle: redgateCompare.sqlserver.options.ignores.ignorePerformanceIndexes
---

## Description

Ignores everything that the 'Ignore indexes' option ignores except primary keys and unique constraints.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.options.ignores.ignorePerformanceIndexes=true
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.ignores]
ignorePerformanceIndexes = true
```
