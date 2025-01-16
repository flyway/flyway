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

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

This can be set from the data comparison options settings in SQL Server projects.

### TOML Configuration File

```toml
[redgateCompare.sqlserver.data.options.deployment]
disableDdlTriggers = true
```
