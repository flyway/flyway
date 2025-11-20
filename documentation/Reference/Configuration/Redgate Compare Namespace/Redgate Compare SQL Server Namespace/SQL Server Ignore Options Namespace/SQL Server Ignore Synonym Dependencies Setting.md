---
subtitle: redgateCompare.sqlserver.options.ignores.ignoreSynonymDependencies
---

## Description

Ignores the names of system named indexes, foreign keys, primary keys, and default, unique, and check constraints when comparing fields in views, tables and table-valued types.

Note that this does not ignore names in view content, for example in a table index hint.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.options.ignores.ignoreSynonymDependencies=true
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.ignores]
ignoreSynonymDependencies = true
```
