---
subtitle: redgateCompare.sqlserver.options.ignores.ignoreComments
---

## Description

Ignores comments when comparing views, stored procedures, and other objects.

Note: The comments will be deployed if the object has other changes and is selected for state-based deployment.

## Type

Boolean

## Default

`false`

## Usage

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.ignores]
ignoreComments = true
```
