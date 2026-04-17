---
subtitle: redgateCompare.oracle.options.behavior.ignoreCommentsInPlSqlBlocks
---

## Description

Ignores comments in PL/SQL blocks when comparing databases.

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
./flyway diff -redgateCompare.oracle.options.ignores.ignoreCommentsInPlSqlBlocks=true
```

### TOML Configuration File

```toml
[redgateCompare.oracle.options.ignores]
ignoreCommentsInPlSqlBlocks = true
```
