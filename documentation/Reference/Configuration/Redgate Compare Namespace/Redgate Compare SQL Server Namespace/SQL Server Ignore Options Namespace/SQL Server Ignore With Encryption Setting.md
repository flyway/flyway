---
subtitle: redgateCompare.sqlserver.options.ignores.ignoreWithEncryption
---

## Description

Ignore `WITH ENCRYPTION` statements on triggers, views, stored procedures and functions.

This option overrides Add `WITH ENCRYPTION`.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.options.ignores.ignoreWithEncryption=true
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.ignores]
ignoreWithEncryption = true
```
