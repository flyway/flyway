---
subtitle: redgateCompare.sqlserver.options.behavior.includeRoleExistenceChecks
---

## Description

Checks for existence of role objects before creating them.

## Type

Boolean

## Default

`true`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.options.behavior.includeRoleExistenceChecks=false
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.behavior]
includeRoleExistenceChecks = false
```
