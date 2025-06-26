---
subtitle: flyway.generate.force
---

## Description

Whether to use placeholders in the generated migration script. If set to `true`, Flyway will attempt to insert placeholders into the script.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### Command-line

```bash
./flyway generate -usePlaceholders=true
```

### TOML Configuration File

```toml
[flyway.generate]
usePlaceholders = true
```
