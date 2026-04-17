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

### Flyway Desktop

This can be set from the data comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.data.options.mapping.includeTimestampColumns=true
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.data.options.mapping]
includeTimestampColumns = true
```
