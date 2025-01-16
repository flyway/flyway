---
subtitle: flyway.generate.timestamp
---

## Description

Generate a timestamp to the calculated version if one is not already present.

## Type

String

### Valid values

- `"always"`
- `"auto"`
- `"never"`

## Default

`"auto"`

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### Command-line

```bash
./flyway generate -timestamp="auto"
```

### TOML Configuration File

```toml
[flyway.generate]
timestamp = "auto"
```
