---
subtitle: redgateCompare.sqlserver.options.behavior.useSetStatementsInScriptDatabaseInfo
---

## Description

Use RedGateDatabaseProperties.xml to store/recover set statements for objects.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.options.behavior.useSetStatementsInScriptDatabaseInfo=true
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.behavior]
useSetStatementsInScriptDatabaseInfo = true
```
