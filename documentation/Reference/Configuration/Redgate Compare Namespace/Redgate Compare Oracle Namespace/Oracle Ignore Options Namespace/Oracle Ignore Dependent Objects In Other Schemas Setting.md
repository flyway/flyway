---
subtitle: redgateCompare.oracle.options.behavior.ignoreDependentObjectsInOtherSchemas
---

## Description

Ignores calculating dependent objects in schemas that haven't been explicitly selected for comparison.

Note: if this option isn't selected, dependencies will be calculated across all schemas, and comparison may be slow.

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
[redgateCompare.oracle.options.ignores]
ignoreDependentObjectsInOtherSchemas = true
```
