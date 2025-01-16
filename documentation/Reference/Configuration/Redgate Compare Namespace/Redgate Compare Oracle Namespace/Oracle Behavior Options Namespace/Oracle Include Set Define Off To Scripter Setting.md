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

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### TOML Configuration File

```toml
[redgateCompare.oracle.options.behavior]
includeSetDefineOffToScripter = true
```
