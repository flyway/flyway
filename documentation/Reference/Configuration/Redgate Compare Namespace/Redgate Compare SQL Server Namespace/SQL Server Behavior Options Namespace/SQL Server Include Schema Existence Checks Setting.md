---
subtitle: redgateCompare.sqlserver.options.behavior.includeSchemaExistenceChecks
---

## Description

Checks for existence of schema objects before creating them. It is recommended not to deselect this option if flyway is configured to create non-existent schemas (true be default).

## Type

Boolean

## Default

`true`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.options.behavior.includeSchemaExistenceChecks=false
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.behavior]
includeSchemaExistenceChecks = false
```
