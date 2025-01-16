---
subtitle: redgateCompare.sqlserver.options.ignores.ignoreBindings
---

## Description

Ignores bindings on columns and user-defined types when comparing and deploying databases. For example, `sp_bindrule` and `sp_bindefault` clauses will be ignored.

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
ignoreBindings = true
```
