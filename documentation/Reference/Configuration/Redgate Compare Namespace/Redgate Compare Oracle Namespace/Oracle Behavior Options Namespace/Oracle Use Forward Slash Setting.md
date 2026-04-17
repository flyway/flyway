---
subtitle: redgateCompare.oracle.options.behavior.useForwardSlash
---

## Description

Terminates each statement in the deployment script with a forward slash.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway diff -redgateCompare.oracle.options.behavior.useForwardSlash=true
```

### TOML Configuration File

```toml
[redgateCompare.oracle.options.behavior]
useForwardSlash = true
```
