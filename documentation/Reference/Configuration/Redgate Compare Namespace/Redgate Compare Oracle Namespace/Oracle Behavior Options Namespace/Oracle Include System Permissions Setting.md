---
subtitle: redgateCompare.oracle.options.behavior.includeSystemPermissions
---

## Description

Add system permissions to the top of the script.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway diff -redgateCompare.oracle.options.behavior.includeSystemPermissions=true
```

### TOML Configuration File

```toml
[redgateCompare.oracle.options.behavior]
includeSystemPermissions = true
```
