---
subtitle: flyway.generate.timestamp
---

## Description

Generate a timestamp to the calculated version if one is not already present.

See [generating migrations](https://documentation.red-gate.com/flyway/database-development-using-flyway/generating-migrations) for more details.

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
