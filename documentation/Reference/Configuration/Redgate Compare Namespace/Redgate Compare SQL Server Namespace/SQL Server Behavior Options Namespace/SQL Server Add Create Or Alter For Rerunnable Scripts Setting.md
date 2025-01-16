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

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.behavior]
addCreateOrAlterForRerunnableScripts = true
```
