---
subtitle: redgateCompare.oracle.options.behavior.ignoreSequenceMaxValue
---

## Description

Ignores the `MAXVALUE` property of sequences only when comparing databases.

Note: if this option is set and you deploy a sequence, the `MAXVALUE` property from the source will still be deployed.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway diff -redgateCompare.oracle.options.ignores.ignoreSequenceMaxValue=true
```

### TOML Configuration File

```toml
[redgateCompare.oracle.options.ignores]
ignoreSequenceMaxValue = true
```
