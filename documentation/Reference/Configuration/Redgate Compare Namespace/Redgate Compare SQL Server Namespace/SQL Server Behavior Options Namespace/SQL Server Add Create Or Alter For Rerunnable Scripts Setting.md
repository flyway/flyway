---
subtitle: redgateCompare.sqlserver.options.behavior.addCreateOrAlterForRerunnableScripts
---

## Description

When this option is selected, `CREATE OR ALTER` statements are used for the following objects:

- Views
- Stored Procedures
- Functions
- Extended Properties
- DDL Triggers
- DML Triggers

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.options.behavior.addCreateOrAlterForRerunnableScripts=true
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.behavior]
addCreateOrAlterForRerunnableScripts = true
```
