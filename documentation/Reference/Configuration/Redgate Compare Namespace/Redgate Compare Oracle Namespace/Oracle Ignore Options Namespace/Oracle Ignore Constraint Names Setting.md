---
subtitle: redgateCompare.oracle.options.behavior.ignoreConstraintNames
---

## Description

Ignores the names of foreign keys and primary keys as well as default, unique, and check constraints when comparing databases.

Note: constraint names won't be ignored when the databases are deployed.

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
ignoreConstraintNames = true
```
