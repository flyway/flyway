---
subtitle: redgateCompare.sqlserver.data.options.deployment.useExistenceChecks
---

## Description

Adds `IF NOT EXISTS` checks around `INSERT` statements so that each row is only inserted if it does not already exist in the target database.

For example:

```sql
IF NOT EXISTS (SELECT 1 FROM [dbo].[TableName] WHERE [KeyColumn] = @KeyValue)
INSERT INTO [dbo].[TableName] ([KeyColumn], ...) VALUES (@KeyValue, ...)
```

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the data comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.data.options.deployment.useExistenceChecks=true
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.data.options.deployment]
useExistenceChecks = true
```
