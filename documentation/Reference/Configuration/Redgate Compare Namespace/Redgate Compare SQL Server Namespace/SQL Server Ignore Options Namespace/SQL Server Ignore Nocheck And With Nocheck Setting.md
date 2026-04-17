---
subtitle: redgateCompare.sqlserver.options.ignores.ignoreNocheckAndWithNocheck
---

## Description

Ignores the `NOCHECK` and `WITH NOCHECK` arguments on foreign keys and check constraints.

When this option is selected:
- constraints are always applied, even when `NOCHECK` and `WITH NOCHECK` are enabled.
- the 'Ignore `WITH NOCHECK`' option is automatically selected.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.options.ignores.ignoreNocheckAndWithNocheck=true
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.ignores]
ignoreNocheckAndWithNocheck = true
```
