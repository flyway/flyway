---
subtitle: flyway.generate.target
---

## Description

The target to generate the script for, must be either `diff.source` or `diff.target` from `flyway diff`.

## Type

String

## Default

Uses initial target if unspecified.

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### Command-line

```bash
./flyway generate -target="schemaModel"
```

### TOML Configuration File

It is unlikely to be desirable to specify this in a config file, but it is possible:

```toml
[flyway.generate]
target = "schemaModel"
```
