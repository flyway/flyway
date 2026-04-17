---
subtitle: redgateCompare.sqlserver.options.ignores.ignoreIdentitySeedAndIncrementValues
---

## Description

Ignores identity seed and increment values when comparing and synchronizing databases.

In the case of memory-optimized tables or script folder targets, identity seed and increment differences will still be deployed if there are other differences between the tables.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.options.ignores.ignoreIdentitySeedAndIncrementValues=true
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.ignores]
ignoreIdentitySeedAndIncrementValues = true
```
