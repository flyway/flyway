---
subtitle: redgateCompare.oracle.options.behavior.includeDependencies
---

## Description

Includes dependent objects when comparing and deploying databases.
For example, if a view depends on a table then the table will be deployed when deploying the view.

## Type

Boolean

## Default

`true`

## Usage

### Flyway Desktop

This cannot currently be configured from Flyway Desktop, although it will be honoured.

### Command-line

```powershell
./flyway model -redgateCompare.oracle.options.behavior.includeDependencies=false
```

### TOML Configuration File

```toml
[redgateCompare.oracle.options.behavior]
includeDependencies = false
```
