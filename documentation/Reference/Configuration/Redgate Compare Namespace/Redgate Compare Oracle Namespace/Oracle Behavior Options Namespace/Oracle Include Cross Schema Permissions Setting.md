---
subtitle: redgateCompare.oracle.options.behavior.includeCrossSchemaPermissions
---

## Description

Include permissions to/from other schemas that referencing to compared schemas or are referenced by compared schemas.

## Type

Boolean

## Default

`true`

## Usage

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### TOML Configuration File

```toml
[redgateCompare.oracle.options.behavior]
includeCrossSchemaPermissions = true
```
