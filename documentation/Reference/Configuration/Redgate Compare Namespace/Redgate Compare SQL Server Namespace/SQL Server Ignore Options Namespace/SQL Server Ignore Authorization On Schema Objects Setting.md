---
subtitle: redgateCompare.sqlserver.options.ignores.ignoreAuthorizationOnSchemaObjects
---

## Description

Ignores authorization clauses on schema-qualified objects when comparing and deploying databases.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.options.ignores.ignoreAuthorizationOnSchemaObjects=true
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.ignores]
ignoreAuthorizationOnSchemaObjects = true
```
