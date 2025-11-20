---
subtitle: redgateCompare.sqlserver.options.ignores.ignoreFileGroupsPartitionSchemesAndPartitionFunctions
---

## Description

Ignores filegroup clauses, partition schemes and partition functions on tables and keys when comparing and deploying databases. Partition schemes and partition functions are not displayed in the comparison results.

## Type

Boolean

## Default

`true`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.options.ignores.ignoreFileGroupsPartitionSchemesAndPartitionFunctions=false
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.ignores]
ignoreFileGroupsPartitionSchemesAndPartitionFunctions = false
```
