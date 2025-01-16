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

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.behavior]
inlineFulltextFields = true
```
