---
subtitle: flyway.generate.force
---

## Description

If the migration script already exists, overwrite it.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### Command-line

```bash
./flyway generate -force=true
```

### TOML Configuration File

```toml
[flyway.generate]
force = true
```
