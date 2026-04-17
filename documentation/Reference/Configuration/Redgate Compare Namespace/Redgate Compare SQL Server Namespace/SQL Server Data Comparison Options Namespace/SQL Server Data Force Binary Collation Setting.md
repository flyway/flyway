---
subtitle: redgateCompare.sqlserver.data.options.comparison.forceBinaryCollation
---

## Description

For all string data types, forces binary collation irrespective of column collation, resulting in a case-sensitive comparison. When this option is selected and the comparison key is a string, this may result in slower performance because the indexes are not used.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the data comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.data.options.comparison.forceBinaryCollation=true
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.data.options.comparison]
forceBinaryCollation = true
```
