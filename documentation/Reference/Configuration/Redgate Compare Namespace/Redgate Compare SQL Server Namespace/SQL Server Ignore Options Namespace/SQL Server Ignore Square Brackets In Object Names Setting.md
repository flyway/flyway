---
subtitle: redgateCompare.sqlserver.options.ignores.ignoreSquareBracketsInObjectNames
---

## Description

Ignore starting and ending square bracket in object names which have been escaped using square brackets. This applies to textual objects such as stored procedures, triggers, etc.

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
[redgateCompare.sqlserver.options.ignores]
ignoreSquareBracketsInObjectNames = true
```
