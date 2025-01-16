---
subtitle: redgateCompare.sqlserver.options.ignores.ignoreReplicationTriggers
---

## Description

Ignores replication triggers when comparing and deploying databases.

## Type

Boolean

## Default

`true`

## Usage

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.ignores]
ignoreReplicationTriggers = true
```
