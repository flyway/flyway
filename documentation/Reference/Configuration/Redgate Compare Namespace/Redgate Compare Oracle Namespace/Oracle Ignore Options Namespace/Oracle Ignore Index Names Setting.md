---
subtitle: redgateCompare.oracle.options.behavior.ignoreIndexNames
---

## Description

Ignores index names for comparison. If two indexes only differ by name, they will be considered identical when compared.

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
ignoreIndexNames = true
```
