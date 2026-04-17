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

### Flyway Desktop

This can be set from the data comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.data.options.deployment.reseedIdentityColumns=true
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.data.options.deployment]
reseedIdentityColumns = true
```
