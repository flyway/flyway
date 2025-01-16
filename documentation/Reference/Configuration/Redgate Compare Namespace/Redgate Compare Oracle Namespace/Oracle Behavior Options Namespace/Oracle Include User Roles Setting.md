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

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### TOML Configuration File

```toml
[redgateCompare.oracle.options.behavior]
includeUserRoles = true
```
