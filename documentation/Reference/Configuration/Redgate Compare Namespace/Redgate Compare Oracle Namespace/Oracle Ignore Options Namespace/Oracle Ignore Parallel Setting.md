---
subtitle: redgateCompare.oracle.options.behavior.ignoreParallel
---

## Description

Ignores differences in the parallel clause on indexes and tables.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway diff -redgateCompare.oracle.options.ignores.ignoreParallel=true
```

### TOML Configuration File

```toml
[redgateCompare.oracle.options.ignores]
ignoreParallel = true
```
