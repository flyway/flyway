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

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.options.ignores.ignoreNotForReplication=false
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.ignores]
ignoreNotForReplication = false
```
