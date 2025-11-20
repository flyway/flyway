---
subtitle: redgateCompare.sqlserver.options.ignores.ignoreSetQuotedIdentifierAndSetAnsiNullsStatements
---

## Description

Ignores these `SET` statements when comparing views, stored procedures, and other objects. This will also remove the initial set statement for these settings and ANSI_PADDING.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.options.ignores.ignoreSetQuotedIdentifierAndSetAnsiNullsStatements=true
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.ignores]
ignoreSetQuotedIdentifierAndSetAnsiNullsStatements = true
```
