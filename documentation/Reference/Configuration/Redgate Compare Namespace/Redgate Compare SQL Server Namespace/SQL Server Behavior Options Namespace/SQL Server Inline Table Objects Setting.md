---
subtitle: redgateCompare.sqlserver.options.behavior.inlineTableObjects
---

## Description

Script out definitions as part of the table's body where possible for primary keys, foreign keys etc. Only some Indexes can be inlined.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.options.behavior.inlineTableObjects=true
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.behavior]
inlineTableObjects = true
```
