---
subtitle: redgateCompare.sqlserver.options.ignores.ignoreNotForReplication
---

## Description

Ignores the `NOT FOR REPLICATION` property in foreign keys, identities, check constraints and triggers.

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
ignoreNotForReplication = true
```
