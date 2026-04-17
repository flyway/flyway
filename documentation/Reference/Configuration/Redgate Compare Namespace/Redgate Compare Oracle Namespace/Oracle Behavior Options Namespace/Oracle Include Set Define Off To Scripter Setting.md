---
subtitle: redgateCompare.oracle.options.behavior.includeSetDefineOffToScripter
---

## Description

Adds SQL*Plus command `SET DEFINE OFF` to the top of repeatable migration script files, so substitution variables aren't used when running the script.

## Type

Boolean

## Default

`true`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway diff -redgateCompare.oracle.options.behavior.includeSetDefineOffToScripter=false
```

### TOML Configuration File

```toml
[redgateCompare.oracle.options.behavior]
includeSetDefineOffToScripter = false
```
