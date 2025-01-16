---
subtitle: redgateCompare.oracle.options.behavior.ignoreSystemPartitions
---

## Description

Ignores system partitions. The option is active if the "all storage options" or "tablespaces and partitioning" option is enabled.

## Type

Boolean

## Default

`false`

The option is active if the "all storage options" or "tablespaces and partitioning" option is enabled.

## Usage

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### TOML Configuration File

```toml
[redgateCompare.oracle.options.ignores]
ignoreSystemPartitions = true
```
