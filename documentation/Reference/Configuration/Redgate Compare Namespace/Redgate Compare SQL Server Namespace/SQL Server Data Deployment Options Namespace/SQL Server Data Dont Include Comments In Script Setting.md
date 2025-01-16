---
subtitle: redgateCompare.sqlserver.data.options.deployment.dontIncludeCommentsInScript
---

## Description

Do not include the comments in the deployment script.

If comments are included, it is easier to locate items in the output.

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
dontIncludeCommentsInScript = true
```
