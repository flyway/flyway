---
subtitle: redgateCompare.sqlserver.options.ignores.ignoreFullTextIndexing
---

## Description

Ignores full-text catalogs and full-text indexes when comparing and deploying databases.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.options.ignores.ignoreFullTextIndexing=true
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.ignores]
ignoreFullTextIndexing = true
```
