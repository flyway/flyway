---
subtitle: redgateCompare.sqlserver.data.options.deployment.disableForeignKeys
---

## Description

Disables then re-enables foreign keys in the deployment script. Note that in some circumstances foreign keys will be dropped and recreated rather than disabled and re-enabled.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the data comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.data.options.deployment.disableForeignKeys=true
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.data.options.deployment]
disableForeignKeys = true
```
