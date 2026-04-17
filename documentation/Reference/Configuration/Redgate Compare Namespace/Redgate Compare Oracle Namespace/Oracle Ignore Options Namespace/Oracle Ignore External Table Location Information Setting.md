---
subtitle: redgateCompare.oracle.options.behavior.ignoreExternalTableLocationInformation
---

## Description

Ignores the external table location information when comparing tables.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway diff -redgateCompare.oracle.options.ignores.ignoreExternalTableLocationInformation=true
```

### TOML Configuration File

```toml
[redgateCompare.oracle.options.ignores]
ignoreExternalTableLocationInformation = true
```
