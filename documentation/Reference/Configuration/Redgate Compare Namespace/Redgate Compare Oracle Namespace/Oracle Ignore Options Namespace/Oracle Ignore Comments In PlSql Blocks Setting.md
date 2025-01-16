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

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### TOML Configuration File

```toml
[redgateCompare.oracle.options.ignores]
ignoreCommentsInPlSqlBlocks = true
```
