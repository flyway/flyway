---
subtitle: redgateCompare.oracle.options.behavior.excludeSchemaNames
---

## Description

Excludes schema names from the deployment script.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway diff -redgateCompare.oracle.options.behavior.excludeSchemaNames=true
```

### TOML Configuration File

```toml
[redgateCompare.oracle.options.behavior]
excludeSchemaNames = true
```
