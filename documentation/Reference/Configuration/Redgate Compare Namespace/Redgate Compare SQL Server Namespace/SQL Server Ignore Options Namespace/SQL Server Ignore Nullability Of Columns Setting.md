---
subtitle: redgateCompare.sqlserver.options.ignores.ignoreNullabilityOfColumns
---

## Description

Ignore whether or not a column can allow `NULL` values.

This means that when you change a column from `NULL` to `NOT NULL` (or vice versa), it won't appear as a difference during comparison, or be deployed as a change.

In the case of memory-optimized tables or script folder targets, nullability differences will still be deployed if there are other differences between the tables.

## Type

Boolean

## Default

`false`

## Usage

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.ignores]
ignoreNullabilityOfColumns = true
```
