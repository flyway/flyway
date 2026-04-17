---
subtitle: redgateCompare.oracle.options.behavior.includeUserRoles
---

## Description

Includes user roles.

Note: User with DBA privileges is needed to include roles to users in script, otherwise roles will not be scripted.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway diff -redgateCompare.oracle.options.behavior.includeUserRoles=true
```

### TOML Configuration File

```toml
[redgateCompare.oracle.options.behavior]
includeUserRoles = true
```
