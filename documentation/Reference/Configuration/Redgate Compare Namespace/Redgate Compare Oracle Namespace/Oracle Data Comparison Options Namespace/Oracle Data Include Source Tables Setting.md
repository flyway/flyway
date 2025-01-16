---
subtitle: redgateCompare.oracle.data.options.comparison.includeSourceTables
---

## Description

Allows data comparison when tables exist in source but not in target.

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
[redgateCompare.oracle.data.options.comparison]
includeSourceTables = true
```
