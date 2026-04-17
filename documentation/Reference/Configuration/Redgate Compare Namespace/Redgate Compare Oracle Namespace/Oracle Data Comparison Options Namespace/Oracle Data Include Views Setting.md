---
subtitle: redgateCompare.oracle.data.options.comparison.includeViews
---

## Description

Includes views in the comparison. Care must be taken when deploying changes to views due to potential issues modifying the underlying table.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway diff -redgateCompare.oracle.data.options.comparison.includeViews=true
```

### TOML Configuration File

```toml
[redgateCompare.oracle.data.options.comparison]
includeViews = true
```
