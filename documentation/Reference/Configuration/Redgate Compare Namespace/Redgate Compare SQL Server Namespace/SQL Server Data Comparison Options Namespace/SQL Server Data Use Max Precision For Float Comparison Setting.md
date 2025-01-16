---
subtitle: redgateCompare.sqlserver.data.options.comparison.useMaxPrecisionForFloatComparison
---

## Description

Select this option if you want to compare floating point values to the maximum 17 digits of precision.

By default, floats are compared to 15 digits of precision.

Note that a value can't be deployed if it is different only in the two additional digits of precision.

## Type

Boolean

## Default

`false`

## Usage

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

This can be set from the data comparison options settings in SQL Server projects.

### TOML Configuration File

```toml
[redgateCompare.sqlserver.data.options.comparison]
useMaxPrecisionForFloatComparison = true
```
