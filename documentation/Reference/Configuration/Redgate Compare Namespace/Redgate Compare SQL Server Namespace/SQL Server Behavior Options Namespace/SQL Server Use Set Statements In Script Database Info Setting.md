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

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.behavior]
useSetStatementsInScriptDatabaseInfo = true
```
