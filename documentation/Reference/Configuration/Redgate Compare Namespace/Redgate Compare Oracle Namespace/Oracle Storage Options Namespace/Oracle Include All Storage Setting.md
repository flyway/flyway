---
subtitle: redgateCompare.oracle.options.storage.includeAllStorage
---

## Description

Includes all storage properties (physical properties) on tables and indexes when comparing and deploying schemas.

Note: when this option is selected, all possible storage clauses are compared and scripted, and all other "Include storage" options are ignored.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway diff -redgateCompare.oracle.options.storage.includeAllStorage=true
```

### TOML Configuration File

```toml
[redgateCompare.oracle.options.storage]
includeAllStorage = true
```
