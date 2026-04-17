---
subtitle: redgateCompare.oracle.options.behavior.ignoreIndexNames
---

## Description

Ignores index names for comparison. If two indexes only differ by name, they will be considered identical when compared.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway diff -redgateCompare.oracle.options.ignores.ignoreIndexNames=true
```

### TOML Configuration File

```toml
[redgateCompare.oracle.options.ignores]
ignoreIndexNames = true
```
