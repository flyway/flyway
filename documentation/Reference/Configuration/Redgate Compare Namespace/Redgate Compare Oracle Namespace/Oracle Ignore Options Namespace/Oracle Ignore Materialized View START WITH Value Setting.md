---
subtitle: redgateCompare.oracle.options.behavior.ignoreMaterializedViewStartWithValue
---

## Description

Ignores materialized view `START WITH` value.

## Type

Boolean

## Default

`true`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway diff -redgateCompare.oracle.options.ignores.ignoreMaterializedViewStartWithValue=false
```

### TOML Configuration File

```toml
[redgateCompare.oracle.options.ignores]
ignoreMaterializedViewStartWithValue = false
```
