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

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### TOML Configuration File

```toml
[redgateCompare.oracle.options.behavior]
excludeSchemaNames = true
```
