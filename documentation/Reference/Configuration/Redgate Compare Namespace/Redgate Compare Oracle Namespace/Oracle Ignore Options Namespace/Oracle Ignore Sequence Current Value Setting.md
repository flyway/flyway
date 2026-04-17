---
subtitle: redgateCompare.oracle.options.behavior.ignoreSequenceCurrentValue
---

## Description

Ignores the current value of sequences when comparing and deploying databases.

If this option is set and you deploy a sequence, the current value of the target sequence is retained. Note: if the current value of the target sequence isn't within the `MINVALUE` and `MAXVALUE` limits of the source sequence, the current value will be reset to a default value.

If this option isn't set and you deploy a sequence, the current value of the source sequence is used as the current value for the target sequence.

## Type

Boolean

## Default

`true`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway diff -redgateCompare.oracle.options.ignores.ignoreSequenceCurrentValue=false
```

### TOML Configuration File

```toml
[redgateCompare.oracle.options.ignores]
ignoreSequenceCurrentValue = false
```
