---
subtitle: redgateCompare.sqlserver.options.behavior.forbidDuplicateTableStorageSettings
---

## Description

Forbids setting file group, file stream, partition scheme, and data compression options on both a table and a clustered index or non-inlined clustered constraint on that table, even though when options are specified on both, the options specified on the table are ignored by SQL Server.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.options.behavior.forbidDuplicateTableStorageSettings=true
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.behavior]
forbidDuplicateTableStorageSettings = true
```
