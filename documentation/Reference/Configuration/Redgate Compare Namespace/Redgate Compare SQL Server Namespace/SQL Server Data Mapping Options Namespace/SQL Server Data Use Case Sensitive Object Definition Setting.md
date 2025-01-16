---
subtitle: redgateCompare.sqlserver.data.options.mapping.useCaseSensitiveObjectDefinition
---

## Description

Treats object names (tables, views, users, roles, schemas, indexes, and columns) as case-sensitive when mapping. For example, [dbo].[Widget] will not be mapped to [dbo].[wIDgEt] when this option is set.

Note that: 
- if the databases that you are comparing are running on a SQL Server that uses case-sensitive sort order, you should ensure that this option is selected.
- if you compare a SQL Azure database with this option selected, false differences may be highlighted.

## Type

Boolean

## Default

`true`

## Usage

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

This can be set from the data comparison options settings in SQL Server projects.

### TOML Configuration File

```toml
[redgateCompare.sqlserver.data.options.mapping]
useCaseSensitiveObjectDefinition = true
```
