---
subtitle: redgateCompare.sqlserver.options.behavior.includeDependencies
---

## Description

Includes dependent objects when comparing and deploying databases. For example, if a view depends on a table then the
table will be deployed when deploying the view.

When this setting is set to true, Flyway Desktop will display the list of dependencies that would be pulled in, and
prompt to select them, though it is possible to deselect them. This applies to all relevant schema model and migrations
operations.

## Type

Boolean

## Default

`true`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

When set to false, dependencies are never pulled in, and the dependency review step is disabled for schema model and
migrations operations.

### Command-line

```powershell
./flyway model -redgateCompare.sqlserver.options.behavior.includeDependencies=false
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.behavior]
includeDependencies = false
```
