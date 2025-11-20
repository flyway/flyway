---
subtitle: redgateCompare.oracle.options.behavior.ignoreLengthSemantics
---

## Description

Ignores length semantics differences for columns.

CHAR and VARCHAR2 columns are stored as CHAR or BYTE depending on the server configuration (NLS_LENGTH_SEMANTICS) or column specification. Normally, if a particular column is stored as CHAR in one schema and BYTE in another, Schema Compare will treat this as a difference.

If this option is set, Schema Compare will not treat it as a difference.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway diff -redgateCompare.oracle.options.ignores.ignoreLengthSemantics=true
```

### TOML Configuration File

```toml
[redgateCompare.oracle.options.ignores]
ignoreLengthSemantics = true
```
