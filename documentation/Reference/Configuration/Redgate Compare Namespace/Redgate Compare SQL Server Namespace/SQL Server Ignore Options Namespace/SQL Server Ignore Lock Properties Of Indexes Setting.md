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

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.options.ignores.ignoreLockPropertiesOfIndexes=true
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.ignores]
ignoreLockPropertiesOfIndexes = true
```
