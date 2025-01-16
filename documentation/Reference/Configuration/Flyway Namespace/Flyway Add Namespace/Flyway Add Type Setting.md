---
subtitle: flyway.add.type
---

## Description

The type of migration to create.

## Type

String

### Valid values

- `"versioned"`
- `"baseline"`
- `"undo"`
- `"repeatable"`

## Default

`"versioned"`

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### Command-line

```bash
./flyway add -type="versioned"
```

### TOML Configuration File

It is unlikely to be desirable to specify this in a configuration file, but it is possible:

```toml
[flyway.add]
type = "versioned"
```
