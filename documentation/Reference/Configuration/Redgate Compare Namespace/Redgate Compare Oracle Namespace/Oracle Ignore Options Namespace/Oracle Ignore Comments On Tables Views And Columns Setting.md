---
subtitle: redgateCompare.oracle.options.behavior.ignoreCommentsOnTablesViewsAndColumns
---

## Description

Ignores comments on tables, views, and columns when comparing databases.

Note: the comments will be deployed if the object has other changes and is selected for state-based deployment.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway diff -redgateCompare.oracle.options.ignores.ignoreCommentsOnTablesViewsAndColumns=true
```

### TOML Configuration File

```toml
[redgateCompare.oracle.options.ignores]
ignoreCommentsOnTablesViewsAndColumns = true
```
