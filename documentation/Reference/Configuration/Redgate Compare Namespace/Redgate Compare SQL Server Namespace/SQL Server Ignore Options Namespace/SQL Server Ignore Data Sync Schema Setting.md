---
subtitle: redgateCompare.sqlserver.options.ignores.ignoreDataSyncSchema
---

## Description

Ignore all objects contained in a schema called DataSync. Data Sync is an Azure feature that temporarily populates a database with objects that you may want to ignore during comparison.

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
[redgateCompare.sqlserver.options.ignores]
ignoreDataSyncSchema = true
```
