---
subtitle: redgateCompare.oracle.options.behavior.ignoreSequenceMaxValue
---

## Description

Ignores the `MAXVALUE` property of sequences only when comparing databases.

Note: if this option is set and you deploy a sequence, the `MAXVALUE` property from the source will still be deployed.

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
ignoreSequenceMaxValue = true
```
