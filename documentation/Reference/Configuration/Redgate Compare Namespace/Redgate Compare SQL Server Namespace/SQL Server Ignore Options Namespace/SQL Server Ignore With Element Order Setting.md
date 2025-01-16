---
subtitle: redgateCompare.sqlserver.options.ignores.ignoreWithElementOrder
---

## Description

If a stored procedure, user-defined function, DDL trigger, DML trigger, or view contains multiple `WITH` elements (such as encryption, schema binding), select this option to ignore the order of the `WITH` elements when comparing and deploying databases.

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
ignoreWithElementOrder = true
```
