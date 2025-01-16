---
subtitle: redgateCompare.sqlserver.options.behavior.useCaseSensitiveObjectDefinition
---

## Description

When this option is on text comparisons on objects will be performed case sensitively. For example, object names such as `ATable` and `atable` will be considered to be different.

You should use this option only if you have databases with binary or case-sensitive sort order.

## Type

Boolean

## Default

`false`

## Usage

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.behavior]
useCaseSensitiveObjectDefinition = true
```
