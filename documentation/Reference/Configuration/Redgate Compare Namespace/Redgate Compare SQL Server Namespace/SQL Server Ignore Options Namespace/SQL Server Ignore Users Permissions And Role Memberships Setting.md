---
subtitle: redgateCompare.sqlserver.options.ignores.ignoreUsersPermissionsAndRoleMemberships
---

## Description

When role-based security is used, object permissions are assigned to roles, not users. If this option is selected, object permissions are compared and deployed only for roles, and members of roles that are roles. Users' permissions and role memberships are ignored.

## Type

Boolean

## Default

`true`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.options.ignores.ignoreUsersPermissionsAndRoleMemberships=false
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.ignores]
ignoreUsersPermissionsAndRoleMemberships = false
```
