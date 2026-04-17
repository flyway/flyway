---
subtitle: redgateCompare.oracle.options.behavior.ignoreSequenceMaxValue
---

## Description

Ignores invisible columns when comparing databases.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway diff -redgateCompare.oracle.options.ignores.ignoreInvisibleColumns=true
```

### TOML Configuration File

```toml
[redgateCompare.oracle.options.ignores]
ignoreInvisibleColumns = true
```
