---
subtitle: redgateCompare.sqlserver.options.behavior.addWithEncryption
---

## Description

Adds `WITH ENCRYPTION` when stored procedures, functions, views, and triggers are included in the deployment.

Note that:
- `WITH ENCRYPTION` isn’t saved in snapshots.
- `WITH ENCRYPTION` isn’t added to natively compiled stored procedures.
- if you select this option, you can’t deploy to a Microsoft Azure SQL database; the deployment script will fail.

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
[redgateCompare.sqlserver.options.behavior]
addWithEncryption = true
```
