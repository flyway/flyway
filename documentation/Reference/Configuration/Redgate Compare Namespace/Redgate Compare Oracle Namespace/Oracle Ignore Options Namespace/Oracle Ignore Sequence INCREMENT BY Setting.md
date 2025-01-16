---
subtitle: redgateCompare.oracle.options.behavior.ignoreSequenceIncrementBy
---

## Description

Ignores the `INCREMENT BY` property of sequences only when comparing databases.

Note: if this option is set and you deploy a sequence, the `INCREMENT BY` property from the source will still be deployed.

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
ignoreSequenceIncrementBy = true
```
