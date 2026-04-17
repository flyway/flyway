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

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.options.ignores.ignoreBindings=true
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.ignores]
ignoreBindings = true
```
