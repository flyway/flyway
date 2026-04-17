---
subtitle: redgateCompare.oracle.data.options.comparison.includeSourceTables
---

## Description

Allows data comparison when tables exist in source but not in target.

## Type

Boolean

## Default

`true`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway diff -redgateCompare.oracle.data.options.comparison.includeSourceTables=false
```

### TOML Configuration File

```toml
[redgateCompare.oracle.data.options.comparison]
includeSourceTables = false
```
