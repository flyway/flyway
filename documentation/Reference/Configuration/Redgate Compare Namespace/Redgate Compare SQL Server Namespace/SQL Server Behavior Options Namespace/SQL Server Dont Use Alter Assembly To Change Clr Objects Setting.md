---
subtitle: redgateCompare.sqlserver.options.behavior.dontUseAlterAssemblyToChangeClrObjects
---

## Description

When this option is selected, any CLR assemblies that are to be deployed will be dropped and re-created, as well as any CLR objects that depend on them, rather than using `ALTER ASSEMBLY`.

If this affects a CLR type, then there will be two rebuilds of any tables that use this type, with conversion to and from strings to update the CLR types.

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
dontUseAlterAssemblyToChangeClrObjects = true
```
