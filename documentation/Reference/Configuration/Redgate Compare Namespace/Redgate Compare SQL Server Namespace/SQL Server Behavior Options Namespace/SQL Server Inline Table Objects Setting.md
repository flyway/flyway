---
subtitle: redgateCompare.sqlserver.options.behavior.inlineTableObjects
---

## Description

Script out definitions as part of the table's body where possible for primary keys, foreign keys etc. Only some Indexes can be inlined.

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
inlineTableObjects = true
```
