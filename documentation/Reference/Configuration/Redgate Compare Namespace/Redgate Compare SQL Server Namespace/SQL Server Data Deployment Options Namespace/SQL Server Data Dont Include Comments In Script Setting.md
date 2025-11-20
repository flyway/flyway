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

### Flyway Desktop

This can be set from the data comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.data.options.deployment.dontIncludeCommentsInScript=true
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.data.options.deployment]
dontIncludeCommentsInScript = true
```
