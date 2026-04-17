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

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway diff -redgateCompare.oracle.data.options.comparison.ignoreDateTypeDifferences=true
```

### TOML Configuration File

```toml
[redgateCompare.oracle.data.options.comparison]
ignoreDateTypeDifferences = true
```
