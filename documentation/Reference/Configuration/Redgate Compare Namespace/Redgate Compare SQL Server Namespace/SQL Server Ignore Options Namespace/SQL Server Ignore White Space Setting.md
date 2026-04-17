---
subtitle: redgateCompare.sqlserver.options.ignores.ignoreWhiteSpace
---

## Description

Ignores whitespace (e.g. newlines, tabs, spaces) when comparing databases. Note that whitespace will not be ignored when the databases are deployed.

This option will not ignore any whitespace differences found within string literals.

This option should be selected when comparing or deploying to or from the schema model.

## Type

Boolean

## Default

`true`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.options.ignores.ignoreWhiteSpace=false
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.ignores]
ignoreWhiteSpace = false
```
