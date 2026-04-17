---
subtitle: redgateCompare.mysql.options.ignores.ignoreNewlinesInTextObjects
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

### Flyway Desktop

This can't currently be set from Flyway Desktop, although it will be honoured.

### Command-line

```powershell
./flyway diff -redgateCompare.mysql.options.ignores.ignoreNewlinesInTextObjects=all
```

### TOML Configuration File

```toml
[redgateCompare.mysql.options.ignores]
ignoreNewlinesInTextObjects = "all"
```
