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

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### TOML Configuration File

```toml
[redgateCompare.oracle.options.ignores]
ignoreDoubleQuotesInPlSqlBlocks = true
```
