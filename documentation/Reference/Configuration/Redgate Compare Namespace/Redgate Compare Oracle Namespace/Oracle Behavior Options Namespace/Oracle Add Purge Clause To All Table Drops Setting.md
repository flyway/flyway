---
subtitle: redgateCompare.oracle.options.behavior.addPurgeClauseToAllTableDrops
---

## Description

Immediately releases the space associated with dropped tables, instead of moving tables and their dependents to the recycle bin.

Note: if this option is selected, you won’t be able to recover dropped tables.

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
addPurgeClauseToAllTableDrops = true
```
