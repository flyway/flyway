---
subtitle: redgateCompare.sqlserver.options.ignores.ignoreFillFactorAndIndexPadding
---

## Description

Ignores the fill factor and index padding in indexes and primary keys when comparing and deploying databases.

## Type

Boolean

## Default

`true`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.options.ignores.ignoreFillFactorAndIndexPadding=false
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.ignores]
ignoreFillFactorAndIndexPadding = false
```
