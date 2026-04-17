---
subtitle: redgateCompare.sqlserver.options.ignores.ignoreIndexes
---

## Description

Ignores indexes, unique constraints and primary keys when comparing and deploying databases.

In the case of memory-optimized tables or script folder targets, index differences will still be deployed if there are other differences between the tables.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.options.ignores.ignoreIndexes=true
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.ignores]
ignoreIndexes = true
```
