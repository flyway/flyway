---
subtitle: redgateCompare.sqlserver.data.options.deployment.transportClrDataTypesAsBinary
---

## Description

Uses the binary representation of CLR types in the deployment script.
Note that:
- if this option is not selected the string representation will be used.
- if you deploy user-defined CLR types to a SQL Azure database, this option has no effect; SQL Azure does not support user-defined CLR types.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the data comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.data.options.deployment.transportClrDataTypesAsBinary=true
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.data.options.deployment]
transportClrDataTypesAsBinary = true
```
