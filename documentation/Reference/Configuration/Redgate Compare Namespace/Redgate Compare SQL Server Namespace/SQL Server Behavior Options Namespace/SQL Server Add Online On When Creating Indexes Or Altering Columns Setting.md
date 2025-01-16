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

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.behavior]
addOnlineOnWhenCreatingIndexesOrAlteringColumns = true
```
