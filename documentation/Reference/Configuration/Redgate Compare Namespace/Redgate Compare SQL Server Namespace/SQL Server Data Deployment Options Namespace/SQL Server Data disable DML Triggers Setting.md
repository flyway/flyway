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

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

This can be set from the data comparison options settings in SQL Server projects.

### TOML Configuration File

```toml
[redgateCompare.sqlserver.data.options.deployment]
disableDmlTriggers = true
```
