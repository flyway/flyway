---
subtitle: redgateCompare.sqlserver.data.options.deployment.skipIntegrityChecksForForeignKeys
---

## Description

Uses `WITH NOCHECK` to skip integrity checks for foreign key constraints. If you select this option:
- deployments may run faster.
- foreign keys will be left in a 'not trusted' state.

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
skipIntegrityChecksForForeignKeys = true
```
