---
subtitle: redgateCompare.sqlserver.options.behavior.addDropAndCreateForRerunnableScripts
---

## Description

When this option is selected, `DROP` and `CREATE` statements are used instead of `ALTER`, and conditional DROP statements are added to CREATE statements, for the following objects:

- Views
- Stored Procedures
- Functions
- Extended Properties
- DDL Triggers
- DML Triggers

Note that if you select this option, the Add object existence checks option will automatically be selected, as this is required to make a functioning script.

Using this option may require additional database objects to also be dropped and recreated if those other objects depend on the selected objects; these operations will be added to the script automatically if dependencies are included.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.options.behavior.addDropAndCreateForRerunnableScripts=true
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.behavior]
addDropAndCreateForRerunnableScripts = true
```
