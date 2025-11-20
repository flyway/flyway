---
subtitle: redgateCompare.sqlserver.data.options.deployment.disableDdlTriggers
---

## Description

Disables then re-enables DDL triggers in the deployment script.

## Type

Boolean

## Default

`true`

## Usage

### Flyway Desktop

This can be set from the data comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.data.options.deployment.disableDdlTriggers=false
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.data.options.deployment]
disableDdlTriggers = false
```
