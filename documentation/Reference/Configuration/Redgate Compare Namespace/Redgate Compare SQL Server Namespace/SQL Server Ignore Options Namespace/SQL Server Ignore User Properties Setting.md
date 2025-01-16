---
subtitle: redgateCompare.sqlserver.options.ignores.ignoreUserProperties
---

## Description

If this option is not selected, user properties are compared, such as the type of user (SQL, Windows, certificate-based, asymmetric key based) and any schema. If a user is selected for deployment, the properties are deployed where possible.

If you select this option, users' properties are ignored, and only the user name is compared and deployed.

## Type

Boolean

## Default

`true`

## Usage

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.ignores]
ignoreUserProperties = true
```
