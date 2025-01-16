---
subtitle: redgateCompare.oracle.data.options.comparison.trimTrailingSpaces
---

## Description

If the data in two columns differs only by the number of spaces at the end of the string, treat it as identical. If this option is selected, trailing spaces will be ignored during deployment.

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
trimTrailingSpaces = true
```
