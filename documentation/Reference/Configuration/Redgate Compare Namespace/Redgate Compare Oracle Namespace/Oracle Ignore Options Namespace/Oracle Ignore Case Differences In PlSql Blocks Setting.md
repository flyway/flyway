---
subtitle: redgateCompare.oracle.options.behavior.ignoreCaseDifferencesInPlSqlBlocks
---

## Description

Ignores case differences in object SQL creation scripts when comparing databases. For example, if you turn this option on, `MYTABLE` and `mytable` aren't considered different table names.

Note: case differences in strings and double quoted identifiers aren't ignored. Case differences won't be ignored when the databases are deployed.

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
ignoreCaseDifferencesInPlSqlBlocks = true
```
