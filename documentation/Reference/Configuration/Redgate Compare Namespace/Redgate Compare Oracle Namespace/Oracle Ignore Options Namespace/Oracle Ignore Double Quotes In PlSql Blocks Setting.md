---
subtitle: redgateCompare.oracle.options.behavior.ignoreDoubleQuotesInPlSqlBlocks
---

## Description

Ignores double quotation marks around identifiers in PL/SQL blocks when comparing databases.

Note: double quotation marks won't be ignored when the databases are deployed.

## Type

Boolean

## Default

`true`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway diff -redgateCompare.oracle.options.ignores.ignoreDoubleQuotesInPlSqlBlocks=false
```

### TOML Configuration File

```toml
[redgateCompare.oracle.options.ignores]
ignoreDoubleQuotesInPlSqlBlocks = false
```
