---
subtitle: redgateCompare.oracle.options.behavior.ignoreSequenceIncrementBy
---

## Description

Ignores the `INCREMENT BY` property of sequences only when comparing databases.

Note: if this option is set and you deploy a sequence, the `INCREMENT BY` property from the source will still be deployed.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway diff -redgateCompare.oracle.options.ignores.ignoreSequenceIncrementBy=true
```

### TOML Configuration File

```toml
[redgateCompare.oracle.options.ignores]
ignoreSequenceIncrementBy = true
```
