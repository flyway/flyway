---
subtitle: redgateCompare.sqlserver.data.options.deployment.dropPrimaryKeysIndexesAndUniqueConstraints
---

## Description

Drops then recreates primary keys, indexes, and unique constraints in the deployment script.

Note that:
- if the primary key, index, or unique constraint is the comparison key, it can't be dropped.
- if the unique constraint is required for a FILESTREAM column, it can't be dropped.
- if you deploy to a SQL Azure database, clustered index constraints are not dropped.

## Type

Boolean

## Default

`false`

## Usage

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

This can be set from the data comparison options settings in SQL Server projects.

### TOML Configuration File

```toml
[redgateCompare.sqlserver.data.options.deployment]
dropPrimaryKeysIndexesAndUniqueConstraints = true
```
