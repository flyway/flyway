---
subtitle: redgateCompare.oracle.options.behavior.includeSetBlankLinesOn
---

## Description

Adds the SQL*Plus command `SET SQLBLANKLINES` ON to the top of the script, so blank lines and new lines are interpreted as part of a SQL command or script.

## Type

Boolean

## Default

`false`

## Usage

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### TOML Configuration File

```toml
[redgateCompare.oracle.options.behavior]
includeSetBlankLinesOn = true
```
