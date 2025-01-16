---
subtitle: redgateCompare.sqlserver.data.options.deployment.reseedIdentityColumns
---

## Description

Reseeds identity values in the target database after deployment. This will try to make the next identity value match the next identity value in the source database.

Note that this will not be applied if there are entries in the target database with a higher identity value than in the source database.

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
reseedIdentityColumns = true
```
