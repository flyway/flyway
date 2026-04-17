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

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway diff -redgateCompare.oracle.options.behavior.suppressDependentAlterCompile=true
```

### TOML Configuration File

```toml
[redgateCompare.oracle.options.behavior]
suppressDependentAlterCompile = true
```
