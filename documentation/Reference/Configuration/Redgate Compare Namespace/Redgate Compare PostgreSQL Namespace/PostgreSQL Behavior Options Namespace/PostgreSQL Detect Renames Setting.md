---
subtitle: redgateCompare.postgresql.options.behavior.detectRenames
---

## Description

Attempt to detect renamed database objects, then deploy renames in the resulting script rather than dropping and creating those objects.

Currently tables and columns are supported. In the future this option may support more object types and matching similar rather than identical objects.

## Type

String

### Valid values

- `"identical-only"`
- `"off"`

## Default

`"identical-only"`

## Usage

### Flyway Desktop

This can't currently be set from Flyway Desktop, although it will be honoured.

### Command-line

```powershell
./flyway diff -redgateCompare.postgresql.options.behavior.detectRenames=off
```

### TOML Configuration File

```toml
[redgateCompare.postgresql.options.behavior]
detectRenames = "off"
```
