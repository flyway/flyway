---
subtitle: redgateCompare.sqlserver.options.ignores.ignoreLockPropertiesOfIndexes
---

## Description

Ignores index `PAGE LOCK` and `ROW LOCK` properties when comparing and deploying databases.

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
ignoreLockPropertiesOfIndexes = true
```
