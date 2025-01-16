---
subtitle: flyway.generate.version
---

## Description

The version part of the migration name.

## Type

String

## Default

<i>calculated automatically based on the migration type</i>

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### Command-line

```bash
./flyway generate -version="001"
```

### TOML Configuration File

It is unlikely to be desirable to specify this in a config file, but it is possible:

```toml
[flyway.generate]
version = "001"
```
