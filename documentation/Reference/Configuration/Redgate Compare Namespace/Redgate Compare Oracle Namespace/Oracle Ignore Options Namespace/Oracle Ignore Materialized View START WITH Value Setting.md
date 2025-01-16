---
subtitle: redgateCompare.oracle.options.behavior.ignoreMaterializedViewStartWithValue
---

## Description

Ignores materialized view `START WITH` value.

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
[redgateCompare.oracle.options.ignores]
ignoreMaterializedViewStartWithValue = true
```
