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

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.options.behavior.forceColumnOrder=true
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.behavior]
forceColumnOrder = true
```
