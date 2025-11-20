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

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.options.ignores.ignoreReplicationTriggers=false
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.ignores]
ignoreReplicationTriggers = false
```
