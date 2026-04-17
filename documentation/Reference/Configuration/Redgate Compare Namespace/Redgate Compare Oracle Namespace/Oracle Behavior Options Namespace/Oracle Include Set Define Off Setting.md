---
subtitle: redgateCompare.oracle.options.behavior.includeSetDefineOff
---

## Description

Adds SQL*Plus command `SET DEFINE OFF` to the top of the script file, so substitution variables aren't used when running the script.

## Type

Boolean

## Default

`true`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway diff -redgateCompare.oracle.options.behavior.includeSetDefineOff=false
```

### TOML Configuration File

```toml
[redgateCompare.oracle.options.behavior]
includeSetDefineOff = false
```
