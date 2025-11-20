---
subtitle: redgateCompare.sqlserver.data.options.deployment.disableDmlTriggers
---

## Description

Disables then re-enables DML triggers on tables and views in the deployment script.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the data comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.data.options.deployment.disableDmlTriggers=true
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.data.options.deployment]
disableDmlTriggers = true
```
