---
subtitle: redgateCompare.sqlserver.options.behavior.addObjectExistenceChecks
---

## Description

When this option is selected, `IF EXISTS` statements are used to check for object existence before modifications in generated scripts.

This option can be useful, for example, if you want to run the deployment script multiple times.

Note that if you deselect this option, the `DROP` and `CREATE` for rerunnable scripts option will also be deselected, as it relies on the checks provided by this option.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.options.behavior.addObjectExistenceChecks=true
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.behavior]
addObjectExistenceChecks = true
```
