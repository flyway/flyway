---
subtitle: redgateCompare.oracle.data.options.comparison.ignoreWhiteSpace
---

## Description

Ignores whitespace differences (newlines, tabs, spaces etc) in text data (clobs, varchars, nvarchars etc).

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway diff -redgateCompare.oracle.data.options.comparison.ignoreWhiteSpace=true
```

### TOML Configuration File

```toml
[redgateCompare.oracle.data.options.comparison]
ignoreWhiteSpace = true
```
