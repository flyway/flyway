---
subtitle: redgateCompare.oracle.options.behavior.ignoreSequenceMaxValue
---

## Description

Ignores invisible indexes when comparing databases.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway diff -redgateCompare.oracle.options.ignores.ignoreInvisibleIndexes=true
```

### TOML Configuration File

```toml
[redgateCompare.oracle.options.ignores]
ignoreInvisibleIndexes = true
```
