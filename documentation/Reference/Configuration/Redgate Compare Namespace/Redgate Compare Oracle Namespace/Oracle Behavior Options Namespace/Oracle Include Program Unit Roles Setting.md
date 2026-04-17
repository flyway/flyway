---
subtitle: redgateCompare.oracle.options.behavior.includeProgramUnitRoles
---

## Description

Includes program unit roles.

Note: user with SYS privileges is needed to include roles to program units in script, otherwise roles will not be scripted.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway diff -redgateCompare.oracle.options.behavior.includeProgramUnitRoles=true
```

### TOML Configuration File

```toml
[redgateCompare.oracle.options.behavior]
includeProgramUnitRoles = true
```
