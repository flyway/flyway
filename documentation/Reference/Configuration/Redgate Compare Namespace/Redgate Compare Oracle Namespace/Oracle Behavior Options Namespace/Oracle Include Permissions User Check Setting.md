---
subtitle: redgateCompare.oracle.options.behavior.includePermissionsUserCheck
---

## Description

Script a condition around GRANTs and REVOKEs to ensure that these statements are skipped if the grantee/revokee is the user running the deployment script.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway generate -redgateCompare.oracle.options.behavior.includePermissionsUserCheck=true
```

### TOML Configuration File

```toml
[redgateCompare.oracle.options.behavior]
includePermissionsUserCheck = true
```
