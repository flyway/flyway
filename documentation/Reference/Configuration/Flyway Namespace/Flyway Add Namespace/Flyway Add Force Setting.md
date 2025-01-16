---
subtitle: flyway.add.force
---

## Description

If the file already exists, overwrite it.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### Command-line

```bash
./flyway add -force=true
```

### TOML Configuration File

```toml
[flyway.add]
force = true
```
