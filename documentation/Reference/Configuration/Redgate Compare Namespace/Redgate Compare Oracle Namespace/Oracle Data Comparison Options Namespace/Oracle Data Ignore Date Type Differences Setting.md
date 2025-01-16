---
subtitle: redgateCompare.oracle.data.options.comparison.ignoreDateTypeDifferences
---

## Description

Ignores data differences in date type (timestamp, date etc) columns.

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
[redgateCompare.oracle.data.options.comparison]
ignoreDateTypeDifferences = true
```
