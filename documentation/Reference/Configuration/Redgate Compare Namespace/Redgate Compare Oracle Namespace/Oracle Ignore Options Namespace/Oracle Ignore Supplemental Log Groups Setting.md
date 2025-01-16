---
subtitle: redgateCompare.oracle.options.behavior.ignoreSupplementalLogGroups
---

## Description

Ignores supplemental log groups when comparing tables.

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
[redgateCompare.oracle.options.ignores]
ignoreSupplementalLogGroups = true
```
