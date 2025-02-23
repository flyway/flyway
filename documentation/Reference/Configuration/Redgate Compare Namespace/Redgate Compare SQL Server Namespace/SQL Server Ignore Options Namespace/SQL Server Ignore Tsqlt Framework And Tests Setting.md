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

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.ignores]
ignoreTsqltFrameworkAndTests = true
```
