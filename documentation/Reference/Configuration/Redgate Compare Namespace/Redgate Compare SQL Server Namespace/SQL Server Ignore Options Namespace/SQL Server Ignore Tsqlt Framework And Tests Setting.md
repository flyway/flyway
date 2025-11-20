---
subtitle: redgateCompare.sqlserver.options.ignores.ignoreTsqltFrameworkAndTests
---

## Description

Ignores the tSQLt schema and its contents, the tSQLt CLR assembly, the SQLCop schema and its contents, and any schemas and their contents with the `tSQLt.TestClass` extended property set.

## Type

Boolean

## Default

`true`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.options.ignores.ignoreTsqltFrameworkAndTests=false
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.ignores]
ignoreTsqltFrameworkAndTests = false
```
