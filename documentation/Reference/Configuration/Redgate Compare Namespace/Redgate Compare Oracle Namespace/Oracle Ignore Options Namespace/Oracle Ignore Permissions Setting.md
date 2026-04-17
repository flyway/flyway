---
subtitle: redgateCompare.oracle.options.behavior.ignorePermissions
---

## Description

Ignores differences in object permissions.

## Type

Boolean

## Default

`true`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway diff -redgateCompare.oracle.options.ignores.ignorePermissions=false
```

### TOML Configuration File

```toml
[redgateCompare.oracle.options.ignores]
ignorePermissions = false
```
