---
subtitle: redgateCompare.oracle.options.behavior.ignoreSupplementalLogGroups
---

## Description

Ignores supplemental log groups when comparing tables.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway diff -redgateCompare.oracle.options.ignores.ignoreSupplementalLogGroups=true
```

### TOML Configuration File

```toml
[redgateCompare.oracle.options.ignores]
ignoreSupplementalLogGroups = true
```
