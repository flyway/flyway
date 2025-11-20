---
subtitle: redgateCompare.sqlserver.options.ignores.ignoreCollations
---

## Description

Ignores collations on character data type columns when comparing and deploying databases.

This option is not used for memory-optimized tables.

## Type

Boolean

## Default

`true`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.options.ignores.ignoreCollations=false
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.ignores]
ignoreCollations = false
```
