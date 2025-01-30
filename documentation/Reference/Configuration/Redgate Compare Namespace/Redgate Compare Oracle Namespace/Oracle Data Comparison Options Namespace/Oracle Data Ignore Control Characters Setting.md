---
subtitle: redgateCompare.oracle.data.options.comparison.ignoreControlCharacters
---

## Description

Ignores control character differences in text data (for example: clobs, varchars, nvarchars etc).

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
ignoreControlCharacters = true
```
