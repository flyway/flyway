---
subtitle: redgateCompare.sqlserver.options.ignores.ignoreDataCompression
---

## Description

Ignores page and row compression when comparing tables and indexes.

When the 'Ignore ignoreFileGroupsPartitionSchemesAndPartitionFunctions' option is selected, compression is ignored for partitioned tables. Objects will be written to the schema model without data compression even if the development database uses data compression.

## Type

Boolean

## Default

`true`

## Usage

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.ignores]
ignoreDataCompression = true
```
