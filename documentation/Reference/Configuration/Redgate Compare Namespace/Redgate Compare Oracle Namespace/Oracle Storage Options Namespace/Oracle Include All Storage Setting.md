---
subtitle: redgateCompare.oracle.options.storage.includeAllStorage
---

## Description

Includes all storage properties (physical properties) on tables and indexes when comparing and deploying schemas.

Note: when this option is selected, all possible storage clauses are compared and scripted, and all other "Include storage" options are ignored.

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
[redgateCompare.oracle.options.storage]
includeAllStorage = true
```
