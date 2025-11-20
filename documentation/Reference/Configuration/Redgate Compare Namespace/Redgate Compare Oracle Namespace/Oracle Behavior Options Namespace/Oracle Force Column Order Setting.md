---
subtitle: redgateCompare.oracle.options.behavior.forceColumnOrder
---

## Description

If deployment requires additional columns to be inserted into the middle of a table, this option forces a rebuild of the table so that column order is preserved following deployment.

The table is rebuilt in four steps:
1. A new table is created.
2. Data from the original table is copied into the new table.
3. The original table is dropped.
4. The new table is renamed as the original table.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway diff -redgateCompare.oracle.options.behavior.forceColumnOrder=true
```

### TOML Configuration File

```toml
[redgateCompare.oracle.options.behavior]
forceColumnOrder = true
```
