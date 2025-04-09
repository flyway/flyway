---
subtitle: flyway.sqlserver.clean.mode
---

## Description

An array of schemas to exclude when [
`flyway.sqlserver.clean.mode`](<Configuration/Flyway Namespace/Flyway SQL Server Namespace/Flyway SQL Server Clean Namespace/Flyway SQL Server Clean Mode Setting>) is set to
`all`.
These schemas won't be dropped or cleaned in `schema` or `all` mode.

## Type

String array

## Default

`[]`

## Usage

### Flyway Desktop

This can't be configured via Flyway Desktop, although it will be honoured.

### TOML Configuration File

```toml
[flyway.sqlserver.clean]
mode = "all"
schemas.exclude = [ "schema1", "schema2" ] 
```

### Configuration File

```properties
flyway.sqlserver.clean.mode=all
flyway.sqlserver.clean.schemas.exclude=schema1,schema2
```
