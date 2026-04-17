---
subtitle: redgateCompare.sqlserver.options.behavior.useDatabaseCompatibilityLevel
---

## Description

Uses a database's compatibility level instead of the SQL Server version.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.options.behavior.useDatabaseCompatibilityLevel=true
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.behavior]
useDatabaseCompatibilityLevel = true
```
