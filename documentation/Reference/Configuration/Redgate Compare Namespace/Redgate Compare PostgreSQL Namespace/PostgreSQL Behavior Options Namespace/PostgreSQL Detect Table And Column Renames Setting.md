---
subtitle: redgateCompare.postgresql.options.behavior.detectTableAndColumnRenames
---

## Description

Automatically detect table and column renames.

## Type

String

### Valid values

- `"identicalOnly"`
- `"off"`

## Default

`"off"`

## Usage

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

This can't currently be set from Flyway Desktop, although it will be honoured.

### TOML Configuration File

```toml
[redgateCompare.postgresql.options.behavior]
detectTableAndColumnRenames = "identicalOnly"
```
