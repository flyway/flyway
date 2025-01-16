---
subtitle: redgateCompare.oracle.options.behavior.suppressDependentAlterCompile
---

## Description

(Experimental)
Turns off all calculations and scripting associated with performing an `ALTER COMPILE` on objects that are dependent on those that are deployed.

Use with caution.

## Type

Boolean

## Default

`false`

## Usage

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### TOML Configuration File

```toml
[redgateCompare.oracle.options.behavior]
suppressDependentAlterCompile = true
```
