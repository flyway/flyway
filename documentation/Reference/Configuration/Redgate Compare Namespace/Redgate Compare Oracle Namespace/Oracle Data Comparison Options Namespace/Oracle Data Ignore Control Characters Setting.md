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

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway diff -redgateCompare.oracle.data.options.comparison.ignoreControlCharacters=true
```

### TOML Configuration File

```toml
[redgateCompare.oracle.data.options.comparison]
ignoreControlCharacters = true
```
