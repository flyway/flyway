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

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway diff -redgateCompare.oracle.options.behavior.includeCrossSchemaPermissions=false
```

### TOML Configuration File

```toml
[redgateCompare.oracle.options.behavior]
includeCrossSchemaPermissions = false
```
