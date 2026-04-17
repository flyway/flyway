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

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.options.behavior.disableAutoColumnMapping=true
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.behavior]
disableAutoColumnMapping = true
```
