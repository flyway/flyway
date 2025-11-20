---
subtitle: redgateCompare.sqlserver.options.ignores.ignoreDataSyncSchema
---

## Description

Ignore all objects contained in a schema called DataSync. Data Sync is an Azure feature that temporarily populates a database with objects that you may want to ignore during comparison.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.options.ignores.ignoreDataSyncSchema=true
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.ignores]
ignoreDataSyncSchema = true
```
