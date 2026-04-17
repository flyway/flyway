---
subtitle: redgateCompare.sqlserver.options.behavior.addOnlineOnWhenCreatingIndexesOrAlteringColumns
---

## Description

Adds the ONLINE = ON option when creating relational indexes or altering columns.

Note that some indexes can't be created with ONLINE = ON within a transaction.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.options.behavior.addOnlineOnWhenCreatingIndexesOrAlteringColumns=true
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.behavior]
addOnlineOnWhenCreatingIndexesOrAlteringColumns = true
```
