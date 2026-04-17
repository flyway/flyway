---
subtitle: redgateCompare.oracle.data.options.comparison.checkTablesForData
---

## Description

Checks each table for data. If either table is empty, no comparison key is required.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway diff -redgateCompare.oracle.data.options.comparison.checkTablesForData=true
```

### TOML Configuration File

```toml
[redgateCompare.oracle.data.options.comparison]
checkTablesForData = true
```
