---
subtitle: redgateCompare.postgresql.options.ignores.ignoreNewlinesInTextObjects
---

## Description

Ignore newlines within text objects when comparing databases.

## Type

String

### Valid values

- `"all"`
- `"off"`

## Default

`"off"`

## Usage

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

This can't currently be set from Flyway Desktop, although it will be honoured.

### TOML Configuration File

```toml
[redgateCompare.postgresql.options.ignores]
ignoreNewlinesInTextObjects = "all"
```
