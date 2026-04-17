---
subtitle: redgateCompare.oracle.options.behavior.ignoreCaseDifferencesInPlSqlBlocks
---

## Description

Ignores case differences in object SQL creation scripts when comparing databases. For example, if you turn this option on, `MYTABLE` and `mytable` aren't considered different table names.

Note: case differences in strings and double quoted identifiers aren't ignored. Case differences won't be ignored when the databases are deployed.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway diff -redgateCompare.oracle.options.ignores.ignoreCaseDifferencesInPlSqlBlocks=true
```

### TOML Configuration File

```toml
[redgateCompare.oracle.options.ignores]
ignoreCaseDifferencesInPlSqlBlocks = true
```
