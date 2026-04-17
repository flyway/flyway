---
subtitle: redgateCompare.sqlserver.options.behavior.inlineFulltextFields
---

## Description

Script out fulltext indexes as a single `CREATE` statement instead of may `ALTER` statements.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.options.behavior.inlineFulltextFields=true
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.behavior]
inlineFulltextFields = true
```
