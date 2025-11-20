---
subtitle: redgateCompare.sqlserver.options.ignores.ignoreExtendedProperties
---

## Description

Ignores extended properties on objects and databases when comparing and deploying databases.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.options.ignores.ignoreExtendedProperties=true
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.ignores]
ignoreExtendedProperties = true
```
