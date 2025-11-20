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

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway diff -redgateCompare.oracle.options.behavior.includeSetBlankLinesOn=true
```

### TOML Configuration File

```toml
[redgateCompare.oracle.options.behavior]
includeSetBlankLinesOn = true
```
